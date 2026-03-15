import { useEffect, useRef, useState } from 'react';
import { BrowserMultiFormatReader } from '@zxing/library';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { Button } from '@components/common/forms/Button';

const QRScanner = ({ onScan, onClose }) => {
  const videoRef = useRef(null);
  const [error, setError] = useState(null);
  const readerRef = useRef(null);

  useEffect(() => {
    const codeReader = new BrowserMultiFormatReader();
    readerRef.current = codeReader;

    const startScanning = async () => {
      try {
        const videoInputDevices = await codeReader.listVideoInputDevices();
        if (videoInputDevices.length === 0) {
          setError('Aucune caméra détectée');
          return;
        }

        const selectedDeviceId = videoInputDevices[0].deviceId;

        codeReader.decodeFromVideoDevice(
          selectedDeviceId,
          videoRef.current,
          (result, _err) => {
            if (result) {
              onScan(result.getText());
            }
          }
        );
      } catch (err) {
        setError('Erreur d\'accès à la caméra');
        console.error(err);
      }
    };

    startScanning();

    return () => {
      if (readerRef.current) {
        readerRef.current.reset();
      }
    };
  }, [onScan]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-75">
      <div className="relative w-full max-w-md">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 z-10 rounded-full bg-white p-2 shadow-lg"
        >
          <XMarkIcon className="h-6 w-6 text-gray-900" />
        </button>

        <div className="overflow-hidden rounded-lg bg-white p-4">
          {error ? (
            <div className="py-8 text-center">
              <p className="text-red-600">{error}</p>
              <Button onClick={onClose} className="mt-4">
                Fermer
              </Button>
            </div>
          ) : (
            <>
              <video
                ref={videoRef}
                className="w-full rounded-lg"
                style={{ maxHeight: '400px' }}
              />
              <p className="mt-4 text-center text-sm text-gray-600">
                Positionnez le QR Code devant la caméra
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default QRScanner;
