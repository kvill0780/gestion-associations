import { useQuery } from '@tanstack/react-query';
import { FolderIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Spinner } from '@components/common/feedback/Spinner';
import { galleryService } from '@api/services/gallery.service';
import { formatDate } from '@utils/formatters';

const AlbumsView = ({ onSelectAlbum }) => {
  const { data: albums, isLoading } = useQuery({
    queryKey: ['albums'],
    queryFn: galleryService.getAlbums
  });

  if (isLoading) return <Spinner size="lg" />;

  if (!albums?.length) {
    return (
      <Card className="p-12 text-center">
        <p className="text-gray-500">Aucun album disponible</p>
      </Card>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {albums.map((album) => (
        <Card
          key={album.event_id}
          className="cursor-pointer hover:shadow-lg transition"
          onClick={() => onSelectAlbum(album.event_id)}
        >
          <div className="p-6">
            <div className="flex items-center gap-4 mb-4">
              <div className="p-3 bg-primary-100 rounded-lg">
                <FolderIcon className="h-8 w-8 text-primary-600" />
              </div>
              <div className="flex-1">
                <h3 className="font-semibold text-gray-900">{album.event?.title}</h3>
                <p className="text-sm text-gray-500">
                  {album.media_count} média{album.media_count > 1 ? 's' : ''}
                </p>
              </div>
            </div>
            <p className="text-xs text-gray-400">
              Mis à jour le {formatDate(album.last_updated)}
            </p>
          </div>
        </Card>
      ))}
    </div>
  );
};

export default AlbumsView;
