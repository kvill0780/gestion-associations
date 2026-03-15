import React from 'react';
import { Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '@store/authStore';
import { authService } from '@api/services/auth.service';
import { toApiError } from '@api/http/toApiError';
import { FEATURES } from '@config/app.config';
import FeatureUnavailable from '@components/common/feedback/FeatureUnavailable';
import { Spinner } from '@components/common/feedback/Spinner';

const AppLayout = React.lazy(() => import('@components/layout/AppLayout'));
const LoginPage = React.lazy(() => import('@features/auth/LoginPage'));
const DashboardPage = React.lazy(() => import('@features/dashboard/DashboardPage'));
const SuperAdminDashboard = React.lazy(() => import('@features/dashboard/SuperAdminDashboard'));
const MembersPage = React.lazy(() => import('@features/members/MembersPage'));
const EventsPage = React.lazy(() => import('@features/events/EventsPage'));
const TransactionsPage = React.lazy(() => import('@features/transactions/TransactionsPage'));
const ContributionsPage = React.lazy(() => import("@features/contributions/ContributionsPage"));
const DocumentsPage = React.lazy(() => import('@features/documents/DocumentsPage'));
const SettingsPage = React.lazy(() => import('@features/settings/SettingsPage'));
const ProfilePage = React.lazy(() => import('@features/profile/ProfilePage'));
const MessagesPage = React.lazy(() => import('@features/messages/MessagesPage'));
const AnnouncementsPage = React.lazy(() => import('@features/announcements/AnnouncementsPage'));
const EventAttendancePage = React.lazy(() => import('@features/events/EventAttendancePage'));
const EventCheckInPage = React.lazy(() => import('@features/events/EventCheckInPage'));
const EventDetailPage = React.lazy(() => import('@features/events/EventDetailPage'));
const GalleryPage = React.lazy(() => import('@features/gallery/GalleryPage'));
const VotesPage = React.lazy(() => import('@features/votes/VotesPage'));

const RouterFallback = () => (
  <div className="min-h-screen flex items-center justify-center bg-gray-50">
    <Spinner size="lg" />
  </div>
);

const ProtectedLayout = () => {
  const { isAuthenticated, user, setUser, logout } = useAuthStore();
  const {
    data: profile,
    isLoading: isProfileLoading,
    isError: isProfileError,
    error: profileError
  } = useQuery({
    queryKey: ['profile'],
    queryFn: authService.getProfile,
    enabled: isAuthenticated && !user,
    retry: false
  });

  React.useEffect(() => {
    if (profile && !user) {
      setUser(profile);
    }
  }, [profile, user, setUser]);

  React.useEffect(() => {
    if (!isProfileError) return;

    const apiError = toApiError(profileError);
    if (apiError.status === 401 || apiError.status === 403) {
      logout();
    }
  }, [isProfileError, profileError, logout]);

  if (isAuthenticated && !user && isProfileLoading) {
    return null;
  }

  if (isAuthenticated && isProfileError) {
    const apiError = toApiError(profileError);
    if (apiError.status === 401 || apiError.status === 403) {
      return <Navigate to="/login" replace />;
    }
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <AppLayout>
      <Outlet />
    </AppLayout>
  );
};

const AppRouter = () => {
  const { isAuthenticated, user } = useAuthStore();
  const dashboardPath = isAuthenticated && user?.isSuperAdmin ? '/system/dashboard' : '/dashboard';

  return (
    <React.Suspense fallback={<RouterFallback />}>
      <Routes>
        {/* Public routes */}
        <Route
          path="/"
          element={<Navigate to={isAuthenticated ? dashboardPath : '/login'} replace />}
        />
        <Route path="/login" element={<LoginPage />} />

        {/* Protected routes */}
        <Route element={<ProtectedLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/system/dashboard" element={<SuperAdminDashboard />} />
          <Route path="/members" element={<MembersPage />} />
          <Route path="/events" element={<EventsPage />} />
          <Route path="/events/:eventId" element={<EventDetailPage />} />
          <Route path="/events/:eventId/attendance" element={<EventAttendancePage />} />
          <Route path="/events/:eventId/checkin" element={<EventCheckInPage />} />
          <Route path="/transactions" element={<TransactionsPage />} />
const ContributionsPage = React.lazy(() => import("@features/contributions/ContributionsPage"));
          <Route path="/contributions" element={<ContributionsPage />} />
          <Route
            path="/documents"
            element={
              FEATURES.documents ? (
                <DocumentsPage />
              ) : (
                <FeatureUnavailable title="Documents indisponibles" />
              )
            }
          />
          <Route
            path="/messages"
            element={
              FEATURES.messages ? (
                <MessagesPage />
              ) : (
                <FeatureUnavailable title="Messages indisponibles" />
              )
            }
          />
          <Route
            path="/announcements"
            element={
              FEATURES.announcements ? (
                <AnnouncementsPage />
              ) : (
                <FeatureUnavailable title="Annonces indisponibles" />
              )
            }
          />
          <Route
            path="/gallery"
            element={
              FEATURES.gallery ? (
                <GalleryPage />
              ) : (
                <FeatureUnavailable title="Galerie indisponible" />
              )
            }
          />
          <Route
            path="/votes"
            element={
              FEATURES.votes ? (
                <VotesPage />
              ) : (
                <FeatureUnavailable title="Votes indisponibles" />
              )
            }
          />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>

        <Route path="*" element={<Navigate to={isAuthenticated ? dashboardPath : '/login'} replace />} />
      </Routes>
    </React.Suspense>
  );
};

export default AppRouter;
