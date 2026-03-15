import { useState } from 'react';
import { PlusIcon, DocumentIcon, TrashIcon, ArrowDownTrayIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Button } from '@components/common/forms/Button';
import { RequiredLabel } from '@components/common/forms/RequiredLabel';
import { Modal } from '@components/common/feedback/Modal';
import { Spinner } from '@components/common/feedback/Spinner';
import { useDocuments, useUploadDocument, useDeleteDocument } from '@hooks/useDocuments';
import { usePermissions } from '@hooks/usePermissions';
import { formatDate } from '@utils/formatters';
import { documentsService } from '@api/services/documents.service';
import toast from 'react-hot-toast';

const DocumentsPage = () => {
  const { data: documents, isLoading } = useDocuments();
  const uploadDocument = useUploadDocument();
  const deleteDocument = useDeleteDocument();
  const { can } = usePermissions();
  const canUploadDocuments = can('documents.upload');
  const canDeleteDocuments = can('documents.delete');

  const [showModal, setShowModal] = useState(false);
  const [uploadData, setUploadData] = useState({ title: '', category: '', file: null });

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (file) setUploadData({ ...uploadData, file });
  };

  const handleUpload = (e) => {
    e.preventDefault();
    if (!canUploadDocuments || !uploadData.file) return;

    const formData = new FormData();
    formData.append('file', uploadData.file);
    formData.append('title', uploadData.title);
    formData.append('category', uploadData.category);

    uploadDocument.mutate(formData, {
      onSuccess: () => {
        setShowModal(false);
        setUploadData({ title: '', category: '', file: null });
      }
    });
  };

  const getFileIcon = (type) => {
    if (type?.includes('pdf')) return '📄';
    if (type?.includes('image')) return '🖼️';
    if (type?.includes('word')) return '📝';
    if (type?.includes('excel')) return '📊';
    return '📎';
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
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
      const extension = getFileExtension(doc.file_type);
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

  if (isLoading) return <Spinner size="lg" />;

  const documentsList = documents?.data || [];
  const groupedDocs = documentsList.reduce((acc, doc) => {
    const cat = doc.category || 'Autres';
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(doc);
    return acc;
  }, {});

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Documents</h2>
          <p className="text-gray-600">Bibliothèque documentaire</p>
        </div>
        {canUploadDocuments ? (
          <Button onClick={() => setShowModal(true)}>
            <PlusIcon className="mr-2 h-5 w-5" />
            Téléverser
          </Button>
        ) : null}
      </div>

      {Object.entries(groupedDocs).map(([category, docs]) => (
        <Card key={category} title={category}>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {docs.map((doc) => (
              <div key={doc.id} className="rounded-lg border p-4 transition-shadow hover:shadow-md">
                <div className="flex items-start space-x-3">
                  <span className="text-3xl">{getFileIcon(doc.file_type)}</span>
                  <div className="min-w-0 flex-1">
                    <h4 className="truncate font-medium text-gray-900">{doc.title}</h4>
                    <p className="mt-1 text-xs text-gray-500">
                      {formatFileSize(doc.file_size)} • {formatDate(doc.created_at)}
                    </p>
                  </div>
                </div>
                <div className="mt-3 flex space-x-2">
                  <Button
                    variant="secondary"
                    className="flex-1 text-xs"
                    onClick={() => handleDownload(doc)}
                  >
                    <ArrowDownTrayIcon className="mr-1 h-4 w-4" />
                    Télécharger
                  </Button>
                  {canDeleteDocuments ? (
                    <Button variant="danger" onClick={() => deleteDocument.mutate(doc.id)} className="text-xs">
                      <TrashIcon className="h-4 w-4" />
                    </Button>
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        </Card>
      ))}

      {documentsList.length === 0 ? (
        <Card>
          <div className="py-12 text-center">
            <DocumentIcon className="mx-auto h-12 w-12 text-gray-400" />
            <p className="mt-2 text-gray-500">Aucun document</p>
          </div>
        </Card>
      ) : null}

      {canUploadDocuments ? (
        <Modal isOpen={showModal} onClose={() => setShowModal(false)} title="Téléverser un document">
          <form onSubmit={handleUpload} className="space-y-4">
            <div>
              <RequiredLabel required>Titre</RequiredLabel>
              <input
                type="text"
                value={uploadData.title}
                onChange={(e) => setUploadData({ ...uploadData, title: e.target.value })}
                className="w-full rounded-md border px-3 py-2"
                required
              />
            </div>

            <div>
              <RequiredLabel required>Catégorie</RequiredLabel>
              <select
                value={uploadData.category}
                onChange={(e) => setUploadData({ ...uploadData, category: e.target.value })}
                className="w-full rounded-md border px-3 py-2"
                required
              >
                <option value="">Sélectionner...</option>
                <option value="Statuts">Statuts</option>
                <option value="PV">PV réunions</option>
                <option value="Rapports">Rapports</option>
                <option value="Finances">Finances</option>
              </select>
            </div>

            <div>
              <RequiredLabel required>Fichier</RequiredLabel>
              <input
                type="file"
                onChange={handleFileChange}
                className="w-full rounded-md border px-3 py-2"
                accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png"
                required
              />
            </div>

            <div className="flex justify-end space-x-3">
              <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
                Annuler
              </Button>
              <Button type="submit" disabled={uploadDocument.isPending}>
                {uploadDocument.isPending ? 'Téléversement...' : 'Téléverser'}
              </Button>
            </div>
          </form>
        </Modal>
      ) : null}
    </div>
  );
};

export default DocumentsPage;
