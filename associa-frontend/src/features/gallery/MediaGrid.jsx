import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { PlayIcon, TrashIcon } from '@heroicons/react/24/solid';
import { Card } from '@components/common/data/Card';
import { Spinner } from '@components/common/feedback/Spinner';
import { galleryService } from '@api/services/gallery.service';
import toast from 'react-hot-toast';

const MediaGrid = ({ media, isLoading, canDelete = false }) => {
  const [selectedMedia, setSelectedMedia] = useState(null);
  const queryClient = useQueryClient();

  const deleteMutation = useMutation({
    mutationFn: async (id) => galleryService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['media']);
      toast.success('Media supprimé');
    }
  });

  const getMediaUrl = (path) => {
    return `${import.meta.env.VITE_API_BASE_URL?.replace('/api', '')}/storage/${path}`;
  };

  if (isLoading) return <Spinner size="lg" />;

  if (!media?.length) {
    return (
      <Card className="p-12 text-center">
        <p className="text-gray-500">Aucun média disponible</p>
      </Card>
    );
  }

  return (
    <>
      <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
        {media.map((item) => (
          <div key={item.id} className="group relative">
            <div
              onClick={() => setSelectedMedia(item)}
              className="aspect-square cursor-pointer overflow-hidden rounded-lg bg-gray-100"
            >
              {item.type === 'photo' ? (
                <img
                  src={getMediaUrl(item.thumbnail_path || item.file_path)}
                  alt={item.caption}
                  className="h-full w-full object-cover transition group-hover:scale-110"
                />
              ) : (
                <div className="relative flex h-full w-full items-center justify-center bg-gray-900">
                  <PlayIcon className="h-12 w-12 text-white" />
                </div>
              )}
            </div>
            {canDelete ? (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  if (confirm('Supprimer ce média ?')) deleteMutation.mutate(item.id);
                }}
                className="absolute right-2 top-2 rounded-lg bg-red-600 p-2 text-white opacity-0 transition group-hover:opacity-100"
              >
                <TrashIcon className="h-4 w-4" />
              </button>
            ) : null}
          </div>
        ))}
      </div>

      {selectedMedia ? (
        <div
          onClick={() => setSelectedMedia(null)}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-90 p-4"
        >
          <div className="w-full max-w-4xl">
            {selectedMedia.type === 'photo' ? (
              <img
                src={getMediaUrl(selectedMedia.file_path)}
                alt={selectedMedia.caption}
                className="h-auto w-full rounded-lg"
              />
            ) : (
              <video controls className="w-full rounded-lg">
                <source src={getMediaUrl(selectedMedia.file_path)} type={selectedMedia.mime_type} />
              </video>
            )}
            {selectedMedia.caption ? (
              <p className="mt-4 text-center text-white">{selectedMedia.caption}</p>
            ) : null}
          </div>
        </div>
      ) : null}
    </>
  );
};

export default MediaGrid;
