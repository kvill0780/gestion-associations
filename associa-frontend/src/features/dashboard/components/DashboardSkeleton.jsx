const SkeletonBlock = ({ className }) => (
  <div className={`rounded-xl bg-slate-200 ${className}`} />
);

const DashboardSkeleton = () => {
  return (
    <div className="space-y-6 animate-pulse">
      <div className="space-y-2">
        <SkeletonBlock className="h-7 w-64" />
        <SkeletonBlock className="h-4 w-96" />
      </div>

      <div className="flex flex-wrap gap-2">
        <SkeletonBlock className="h-10 w-40" />
        <SkeletonBlock className="h-10 w-40" />
        <SkeletonBlock className="h-10 w-40" />
      </div>

      <SkeletonBlock className="h-6 w-56" />
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <SkeletonBlock className="h-40" />
        <SkeletonBlock className="h-40" />
      </div>

      <SkeletonBlock className="h-6 w-56" />
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <SkeletonBlock className="h-56" />
        <SkeletonBlock className="h-56" />
      </div>
    </div>
  );
};

export default DashboardSkeleton;
