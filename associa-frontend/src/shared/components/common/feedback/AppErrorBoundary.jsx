import React from 'react';
import { Button } from '@components/common/forms/Button';

export class AppErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    // Keep trace in devtools/console for diagnosis.
    console.error('Unhandled UI error:', error, errorInfo);
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (!this.state.hasError) {
      return this.props.children;
    }

    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100 px-6">
        <div className="w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Associa</p>
          <h1 className="mt-2 text-2xl font-extrabold text-slate-900">Une erreur est survenue</h1>
          <p className="mt-2 text-sm text-slate-600">
            L&apos;application a rencontre un probleme inattendu. Rechargez la page pour reprendre.
          </p>
          <div className="mt-6 flex items-center gap-3">
            <Button onClick={this.handleReload}>Recharger</Button>
            <Button variant="secondary" onClick={() => window.location.assign('/dashboard')}>
              Retour au dashboard
            </Button>
          </div>
        </div>
      </div>
    );
  }
}
