import { Card } from '@components/common/data/Card';

const FeatureUnavailable = ({ title = 'Module indisponible', description }) => {
  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <Card className="max-w-lg text-center">
        <div className="p-8">
          <h2 className="mb-2 text-2xl font-bold text-gray-900">{title}</h2>
          <p className="text-gray-600">
            {description || "Ce module n'est pas encore disponible dans l'API backend en dev."}
          </p>
        </div>
      </Card>
    </div>
  );
};

export default FeatureUnavailable;
