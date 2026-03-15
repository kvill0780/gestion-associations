import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  PlusIcon,
  MegaphoneIcon,
  TrashIcon,
  HandThumbUpIcon,
  HandThumbDownIcon,
  CheckCircleIcon
} from '@heroicons/react/24/outline';
import {
  HandThumbUpIcon as HandThumbUpSolid,
  HandThumbDownIcon as HandThumbDownSolid
} from '@heroicons/react/24/solid';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { Button } from '@components/common/forms/Button';
import { Input } from '@components/common/forms/Input';
import { RequiredLabel } from '@components/common/forms/RequiredLabel';
import { Modal } from '@components/common/feedback/Modal';
import { Spinner } from '@components/common/feedback/Spinner';
import { useAnnouncements, useCreateAnnouncement, useDeleteAnnouncement } from '@hooks/useAnnouncements';
import { usePermissions } from '@hooks/usePermissions';
import { formatDateTime } from '@utils/formatters';
import { announcementsService } from '@api/services/announcements.service';
import { useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';

const announcementSchema = z.object({
  title: z.string().min(3, 'Minimum 3 caractères'),
  content: z.string().min(10, 'Minimum 10 caractères'),
  priority: z.enum(['low', 'normal', 'high', 'urgent']),
  type: z.enum(['announcement', 'poll']),
  poll_question: z.string().optional(),
  poll_options: z.array(z.string()).optional(),
  allow_multiple_votes: z.boolean().optional()
});

const priorityConfig = {
  low: { label: 'Faible', variant: 'default', color: 'bg-gray-100' },
  normal: { label: 'Normal', variant: 'info', color: 'bg-blue-100' },
  high: { label: 'Élevée', variant: 'warning', color: 'bg-yellow-100' },
  urgent: { label: 'Urgent', variant: 'danger', color: 'bg-red-100' }
};

const AnnouncementsPage = () => {
  const { data: announcements, isLoading } = useAnnouncements();
  const createAnnouncement = useCreateAnnouncement();
  const deleteAnnouncement = useDeleteAnnouncement();
  const queryClient = useQueryClient();
  const { can } = usePermissions();

  const canCreateAnnouncements = can('announcements.create');
  const canDeleteAnnouncements = can('announcements.delete');

  const [showModal, setShowModal] = useState(false);
  const [pollOptions, setPollOptions] = useState(['', '']);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors }
  } = useForm({
    resolver: zodResolver(announcementSchema),
    defaultValues: { priority: 'normal', type: 'announcement', allow_multiple_votes: false }
  });

  const watchType = watch('type');

  const onSubmit = (data) => {
    if (!canCreateAnnouncements) return;

    const payload = { ...data };
    if (data.type === 'poll') {
      payload.poll_options = pollOptions.filter((o) => o.trim());
    }

    createAnnouncement.mutate(payload, {
      onSuccess: () => {
        setShowModal(false);
        reset();
        setPollOptions(['', '']);
      }
    });
  };

  const handleReaction = async (announcementId, type, currentReaction) => {
    try {
      if (currentReaction === type) {
        await announcementsService.unreact(announcementId);
      } else {
        await announcementsService.react(announcementId, type);
      }
      queryClient.invalidateQueries(['announcements']);
    } catch {
      toast.error('Erreur');
    }
  };

  const handleVote = async (announcementId, optionId) => {
    try {
      await announcementsService.vote(announcementId, optionId);
      queryClient.invalidateQueries(['announcements']);
      toast.success('Vote enregistré');
    } catch {
      toast.error('Erreur');
    }
  };

  if (isLoading) return <Spinner size="lg" />;

  const announcementsList = announcements?.data || [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Annonces</h2>
          <p className="text-gray-600">Communications officielles de l'association</p>
        </div>
        {canCreateAnnouncements ? (
          <Button onClick={() => setShowModal(true)}>
            <PlusIcon className="mr-2 h-5 w-5" />
            Nouvelle annonce
          </Button>
        ) : null}
      </div>

      <div className="space-y-4">
        {announcementsList.length === 0 ? (
          <Card>
            <div className="py-12 text-center text-gray-500">
              <MegaphoneIcon className="mx-auto mb-4 h-16 w-16 text-gray-300" />
              <p className="text-lg">Aucune annonce pour le moment</p>
            </div>
          </Card>
        ) : (
          announcementsList.map((announcement) => (
            <Card key={announcement.id} className={priorityConfig[announcement.priority].color}>
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="mb-2 flex items-center gap-3">
                    <h3 className="text-lg font-bold text-gray-900">{announcement.title}</h3>
                    <Badge variant={priorityConfig[announcement.priority].variant}>
                      {priorityConfig[announcement.priority].label}
                    </Badge>
                    {announcement.type === 'poll' ? <Badge variant="info">Sondage</Badge> : null}
                  </div>
                  <p className="mb-3 whitespace-pre-wrap text-gray-700">{announcement.content}</p>

                  {announcement.type === 'poll' && announcement.poll_options ? (
                    <div className="mb-4 space-y-2">
                      <p className="font-medium text-gray-900">{announcement.poll_question}</p>
                      {announcement.poll_options.map((option) => {
                        const totalVotes = announcement.poll_options.reduce((sum, o) => sum + o.votes_count, 0);
                        const percentage = totalVotes > 0 ? Math.round((option.votes_count / totalVotes) * 100) : 0;

                        return (
                          <button
                            key={option.id}
                            onClick={() => handleVote(announcement.id, option.id)}
                            className={`w-full rounded-lg border p-3 text-left transition ${
                              option.user_voted
                                ? 'border-primary-500 bg-primary-50'
                                : 'border-gray-300 hover:border-primary-300'
                            }`}
                          >
                            <div className="mb-1 flex items-center justify-between">
                              <span className="font-medium">{option.option_text}</span>
                              {option.user_voted ? <CheckCircleIcon className="h-5 w-5 text-primary-600" /> : null}
                            </div>
                            <div className="flex items-center gap-2">
                              <div className="h-2 flex-1 overflow-hidden rounded-full bg-gray-200">
                                <div className="h-full bg-primary-600" style={{ width: `${percentage}%` }} />
                              </div>
                              <span className="text-sm text-gray-600">
                                {percentage}% ({option.votes_count})
                              </span>
                            </div>
                          </button>
                        );
                      })}
                    </div>
                  ) : null}

                  {announcement.type === 'announcement' ? (
                    <div className="mb-3 flex items-center gap-3">
                      <button
                        onClick={() => handleReaction(announcement.id, 'like', announcement.user_reaction)}
                        className="flex items-center gap-1 rounded-lg px-3 py-1 transition hover:bg-gray-100"
                      >
                        {announcement.user_reaction === 'like' ? (
                          <HandThumbUpSolid className="h-5 w-5 text-primary-600" />
                        ) : (
                          <HandThumbUpIcon className="h-5 w-5 text-gray-600" />
                        )}
                        <span className="text-sm font-medium">{announcement.likes_count || 0}</span>
                      </button>
                      <button
                        onClick={() => handleReaction(announcement.id, 'dislike', announcement.user_reaction)}
                        className="flex items-center gap-1 rounded-lg px-3 py-1 transition hover:bg-gray-100"
                      >
                        {announcement.user_reaction === 'dislike' ? (
                          <HandThumbDownSolid className="h-5 w-5 text-red-600" />
                        ) : (
                          <HandThumbDownIcon className="h-5 w-5 text-gray-600" />
                        )}
                        <span className="text-sm font-medium">{announcement.dislikes_count || 0}</span>
                      </button>
                    </div>
                  ) : null}

                  <div className="flex items-center gap-4 text-sm text-gray-500">
                    <span>Par {announcement.author?.name || 'Inconnu'}</span>
                    <span>•</span>
                    <span>{formatDateTime(announcement.created_at)}</span>
                  </div>
                </div>
                {canDeleteAnnouncements ? (
                  <Button
                    variant="danger"
                    onClick={() => deleteAnnouncement.mutate(announcement.id)}
                    className="ml-4"
                  >
                    <TrashIcon className="h-5 w-5" />
                  </Button>
                ) : null}
              </div>
            </Card>
          ))
        )}
      </div>

      {canCreateAnnouncements ? (
        <Modal isOpen={showModal} onClose={() => setShowModal(false)} title="Nouvelle annonce" size="lg">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <RequiredLabel required>Type</RequiredLabel>
              <select
                {...register('type')}
                className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
                required
              >
                <option value="announcement">Annonce</option>
                <option value="poll">Sondage</option>
              </select>
            </div>

            <Input label="Titre" required {...register('title')} error={errors.title?.message} />

            <div>
              <RequiredLabel required>Contenu</RequiredLabel>
              <textarea
                {...register('content')}
                rows={3}
                required
                className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
              {errors.content ? <p className="mt-1 text-sm text-red-600">{errors.content.message}</p> : null}
            </div>

            {watchType === 'poll' ? (
              <>
                <Input
                  label="Question du sondage"
                  {...register('poll_question')}
                  placeholder="Quelle est votre question ?"
                />

                <div>
                  <RequiredLabel required className="mb-2">
                    Options
                  </RequiredLabel>
                  {pollOptions.map((option, idx) => (
                    <div key={idx} className="mb-2 flex gap-2">
                      <input
                        type="text"
                        value={option}
                        onChange={(e) => {
                          const newOptions = [...pollOptions];
                          newOptions[idx] = e.target.value;
                          setPollOptions(newOptions);
                        }}
                        placeholder={`Option ${idx + 1}`}
                        className="flex-1 rounded-md border border-gray-300 px-3 py-2"
                        required
                      />
                      {pollOptions.length > 2 ? (
                        <Button
                          type="button"
                          variant="danger"
                          onClick={() => setPollOptions(pollOptions.filter((_, i) => i !== idx))}
                        >
                          ×
                        </Button>
                      ) : null}
                    </div>
                  ))}
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={() => setPollOptions([...pollOptions, ''])}
                    className="text-sm"
                  >
                    + Ajouter une option
                  </Button>
                </div>

                <label className="flex items-center gap-2">
                  <input type="checkbox" {...register('allow_multiple_votes')} className="rounded" />
                  <span className="text-sm text-gray-700">Autoriser plusieurs votes</span>
                </label>
              </>
            ) : null}

            <div>
              <RequiredLabel required>Priorité</RequiredLabel>
              <select
                {...register('priority')}
                className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
                required
              >
                <option value="low">Faible</option>
                <option value="normal">Normal</option>
                <option value="high">Élevée</option>
                <option value="urgent">Urgent</option>
              </select>
            </div>

            <div className="flex justify-end gap-3">
              <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
                Annuler
              </Button>
              <Button type="submit" disabled={createAnnouncement.isPending}>
                Publier
              </Button>
            </div>
          </form>
        </Modal>
      ) : null}
    </div>
  );
};

export default AnnouncementsPage;
