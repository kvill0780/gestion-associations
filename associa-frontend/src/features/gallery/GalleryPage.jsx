import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PhotoIcon, FolderIcon } from '@heroicons/react/24/outline';
import { Button } from '@components/common/forms/Button';
import { usePermissions } from '@hooks/usePermissions';
import { galleryService } from '@api/services/gallery.service';
import UploadMediaModal from './UploadMediaModal';
import MediaGrid from './MediaGrid';
import AlbumsView from './AlbumsView';

const GalleryPage = () => {
  const [view, setView] = useState('all');
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const { can } = usePermissions();

  const canUploadGallery = can('gallery.upload');
  const canDeleteGallery = can('gallery.delete');

  const { data: media, isLoading } = useQuery({
    queryKey: ['media', selectedEvent],
    queryFn: () => galleryService.getMedia({ eventId: selectedEvent })
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Galerie</h2>
          <p className="text-gray-600">Photos et vidéos de l'association</p>
        </div>
        {canUploadGallery ? (
          <Button onClick={() => setUploadModalOpen(true)}>
            <PhotoIcon className="mr-2 h-5 w-5" />
            Ajouter des médias
          </Button>
        ) : null}
      </div>

      <div className="flex gap-4">
        <Button variant={view === 'all' ? 'primary' : 'secondary'} onClick={() => setView('all')}>
          <PhotoIcon className="mr-2 h-5 w-5" />
          Tous les médias
        </Button>
        <Button variant={view === 'albums' ? 'primary' : 'secondary'} onClick={() => setView('albums')}>
          <FolderIcon className="mr-2 h-5 w-5" />
          Albums
        </Button>
      </div>

      {view === 'all' ? (
        <MediaGrid media={media?.data || []} isLoading={isLoading} canDelete={canDeleteGallery} />
      ) : (
        <AlbumsView
          onSelectAlbum={(eventId) => {
            setSelectedEvent(eventId);
            setView('all');
          }}
        />
      )}

      {canUploadGallery ? (
        <UploadMediaModal isOpen={uploadModalOpen} onClose={() => setUploadModalOpen(false)} />
      ) : null}
    </div>
  );
};

export default GalleryPage;
