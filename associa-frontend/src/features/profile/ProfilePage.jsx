import { PageHeader } from '@components/common/data/PageHeader';
import ProfileContent from './components/ProfileContent';

const ProfilePage = () => {
  return (
    <div className="space-y-6">
      <PageHeader title="Mon profil" subtitle="Informations personnelles et sécurité du compte" />
      <ProfileContent />
    </div>
  );
};

export default ProfilePage;
