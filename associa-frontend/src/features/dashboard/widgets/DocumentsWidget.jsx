import { Card } from '@components/common/data/Card';
import { DocumentTextIcon, ArrowDownTrayIcon } from '@heroicons/react/24/outline';
import { Button } from '@components/common/forms/Button';
import { useNavigate } from 'react-router-dom';
import { documentsService } from '@api/services/documents.service';
import { formatDate } from '@utils/formatters';
import toast from 'react-hot-toast';

/**
 * Widget affichant les documents récents
 * Visible pour: documents.view, documents_all
 */
const DocumentsWidget = ({ data }) => {
    const navigate = useNavigate();
    const stats = data || {};
    const docsCount = stats.documents?.total || 0;
    const recentDocs = stats.documents?.recent || [];

    const getFileIcon = (type) => {
        if (type?.includes('pdf')) return '📄';
        if (type?.includes('image')) return '🖼️';
        if (type?.includes('word')) return '📝';
        if (type?.includes('excel')) return '📊';
        return '📎';
    };

    const getFileExtension = (type) => {
        if (!type) return '';
        if (type.includes('pdf')) return '.pdf';
        if (type.includes('msword')) return '.doc';
        if (type.includes('officedocument.wordprocessingml')) return '.docx';
        if (type.includes('excel')) return '.xls';
        if (type.includes('officedocument.spreadsheetml')) return '.xlsx';
        if (type.includes('png')) return '.png';
        if (type.includes('jpeg') || type.includes('jpg')) return '.jpg';
        return '';
    };

    const handleDownload = async (doc) => {
        try {
            const blob = await documentsService.download(doc.id);
            const extension = getFileExtension(doc.fileType);
            const filename = `${doc.title || 'document'}${extension}`;
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = filename;
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            toast.error('Téléchargement impossible');
        }
    };

    return (
        <Card title="Documents" badge={docsCount}>
            <div className="space-y-3">
                {docsCount > 0 ? (
                    <>
                        <div className="text-center py-2">
                            <DocumentTextIcon className="h-10 w-10 text-yellow-500 mx-auto mb-2" />
                            <p className="text-sm text-gray-600">
                                {docsCount} document{docsCount > 1 ? 's' : ''} disponible{docsCount > 1 ? 's' : ''}
                            </p>
                        </div>

                        {recentDocs.length > 0 ? (
                            <div className="space-y-2">
                                {recentDocs.map((doc) => (
                                    <div key={doc.id} className="flex items-center justify-between rounded-lg border px-3 py-2">
                                        <div className="flex items-center gap-2 min-w-0">
                                            <span className="text-lg">{getFileIcon(doc.fileType)}</span>
                                            <div className="min-w-0">
                                                <p className="truncate text-sm font-medium text-gray-900">{doc.title}</p>
                                                <p className="text-xs text-gray-500">
                                                    {doc.category || 'Autres'} • {formatDate(doc.createdAt)}
                                                </p>
                                            </div>
                                        </div>
                                        <Button
                                            variant="secondary"
                                            size="sm"
                                            className="text-xs"
                                            onClick={() => handleDownload(doc)}
                                        >
                                            <ArrowDownTrayIcon className="h-4 w-4" />
                                        </Button>
                                    </div>
                                ))}
                            </div>
                        ) : null}

                        <Button
                            variant="secondary"
                            size="sm"
                            onClick={() => navigate('/documents')}
                        >
                            Voir tous les documents
                        </Button>
                    </>
                ) : (
                    <div className="text-center py-4">
                        <DocumentTextIcon className="h-12 w-12 text-gray-400 mx-auto mb-2" />
                        <p className="text-sm text-gray-600">Aucun document</p>
                    </div>
                )}
            </div>
        </Card>
    );
};

export default DocumentsWidget;
