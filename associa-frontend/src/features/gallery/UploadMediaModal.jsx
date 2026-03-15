import { useState } from 'react';
import { useMutation, useQueryClient, useQuery } from '@tanstack/react-query';
import { Modal } from '@components/common/feedback/Modal';
import { Button } from '@components/common/forms/Button';
import { RequiredLabel } from '@components/common/forms/RequiredLabel';
import { usePermissions } from '@hooks/usePermissions';
import { galleryService } from '@api/services/gallery.service';
import { eventsService } from '@api/services/events.service';
import toast from 'react-hot-toast';

const UploadMediaModal = ({ isOpen, onClose }) => {
  const [files, setFiles] = useState([]);
  const [eventId, setEventId] = useState('');
  const [caption, setCaption] = useState('');
  const queryClient = useQueryClient();
  const { can } = usePermissions();
  const canUploadGallery = can('gallery.upload');

  const { data: events } = useQuery({
    queryKey: ['events'],
    queryFn: async () => {
      const result = await eventsService.getAll();
      return result.data || [];
    },
    enabled: isOpen
  });

  const uploadMutation = useMutation({
    mutationFn: galleryService.upload,
    onSuccess: () => {
      queryClient.invalidateQueries(['media']);
      toast.success('Médias uploadés avec succès');
      setFiles([]);
      setEventId('');
      setCaption('');
      onClose();
    },
    onError: () => toast.error("Erreur lors de l'upload")
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!canUploadGallery) return;
    uploadMutation.mutate({ files, eventId, caption });
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Ajouter des médias">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <RequiredLabel required className="mb-2 block text-sm font-medium text-gray-700">
            Fichiers (photos/vidéos)
          </RequiredLabel>
          <input
            type="file"
            multiple
            accept="image/*,video/*"
            onChange={(e) => setFiles(Array.from(e.target.files || []))}
            className="block w-full text-sm text-gray-500 file:mr-4 file:rounded-lg file:border-0 file:bg-primary-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-primary-700 hover:file:bg-primary-100"
            required
          />
          {files.length > 0 ? (
            <p className="mt-2 text-sm text-gray-600">{files.length} fichier(s) sélectionné(s)</p>
          ) : null}
        </div>

        <div>
          <RequiredLabel className="mb-2 block text-sm font-medium text-gray-700">Album (événement)</RequiredLabel>
          <select
            value={eventId}
            onChange={(e) => setEventId(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-4 py-2"
          >
            <option value="">Aucun album</option>
            {events && Array.isArray(events)
              ? events.map((event) => (
                  <option key={event.id} value={event.id}>
                    {event.title}
                  </option>
                ))
              : null}
          </select>
        </div>

        <div>
          <RequiredLabel className="mb-2 block text-sm font-medium text-gray-700">Légende</RequiredLabel>
          <textarea
            value={caption}
            onChange={(e) => setCaption(e.target.value)}
            rows={3}
            className="w-full rounded-lg border border-gray-300 px-4 py-2"
          />
        </div>

        <div className="flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={onClose}>
            Annuler
          </Button>
          <Button type="submit" disabled={!canUploadGallery || files.length === 0 || uploadMutation.isPending}>
            {uploadMutation.isPending ? 'Upload...' : 'Uploader'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default UploadMediaModal;
