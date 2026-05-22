@extends('admin.layout')

@section('title', 'Tableau de bord')

@section('body')
<div class="admin-shell">
    <aside class="admin-sidebar">
        <div class="admin-brand">Smart City <span>Admin</span></div>
        <div class="admin-subtitle">Commune de Casablanca — Pilotage</div>
        <nav class="admin-nav">
            <a href="{{ route('admin.dashboard') }}" class="active">Signalements</a>
            <a href="{{ url('/api/v1/reports') }}" target="_blank" rel="noopener">API JSON ↗</a>
        </nav>
        <form method="POST" action="{{ route('admin.logout') }}" style="margin-top:32px;">
            @csrf
            <button type="submit" class="btn-ghost">Déconnexion</button>
        </form>
    </aside>

    <main class="admin-main">
        <div class="admin-header">
            <h1>Signalements citoyens</h1>
            <span style="color: var(--muted); font-size: 0.9rem;">{{ auth()->user()->name }}</span>
        </div>

        @if (session('success'))
            <div class="alert-success">{{ session('success') }}</div>
        @endif

        <div class="bento">
            <div class="glass-card stat-card total">
                <div class="label">Total</div>
                <div class="value">{{ $stats['total'] }}</div>
            </div>
            <div class="glass-card stat-card pending">
                <div class="label">En attente</div>
                <div class="value">{{ $stats['pending'] }}</div>
            </div>
            <div class="glass-card stat-card progress">
                <div class="label">En cours</div>
                <div class="value">{{ $stats['in_progress'] }}</div>
            </div>
            <div class="glass-card stat-card resolved">
                <div class="label">Résolus</div>
                <div class="value">{{ $stats['resolved'] }}</div>
            </div>
        </div>

        <div class="glass-card table-card">
            <div class="table-toolbar">
                <strong>Liste des signalements</strong>
                <div class="filter-chips">
                    <a href="{{ route('admin.dashboard') }}" class="chip {{ !$statusFilter ? 'active' : '' }}">Tous</a>
                    <a href="{{ route('admin.dashboard', ['status' => 'pending']) }}" class="chip {{ $statusFilter === 'pending' ? 'active' : '' }}">En attente</a>
                    <a href="{{ route('admin.dashboard', ['status' => 'in_progress']) }}" class="chip {{ $statusFilter === 'in_progress' ? 'active' : '' }}">En cours</a>
                    <a href="{{ route('admin.dashboard', ['status' => 'resolved']) }}" class="chip {{ $statusFilter === 'resolved' ? 'active' : '' }}">Résolus</a>
                </div>
            </div>

            <table class="admin-table">
                <thead>
                    <tr>
                        <th>N°</th>
                        <th>Photo</th>
                        <th>Titre</th>
                        <th>Catégorie IA</th>
                        <th>Citoyen</th>
                        <th>Statut</th>
                        <th>Modifier</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse ($reports as $report)
                        <tr>
                            <td>#{{ $report->id }}</td>
                            <td>
                                @if ($report->image_url)
                                    <img src="{{ $report->image_url }}" alt="Photo du signalement" class="thumb">
                                @else
                                    <div class="thumb" aria-label="Sans photo"></div>
                                @endif
                            </td>
                            <td>
                                <strong>{{ $report->title }}</strong><br>
                                <small style="color: var(--muted)">{{ Str::limit($report->description, 60) }}</small>
                            </td>
                            <td>
                                <span class="badge badge-{{ $report->category }}">{{ \App\Support\DisplayLabels::category($report->category) }}</span>
                            </td>
                            <td>
                                {{ $report->user?->name ?? '—' }}<br>
                                <small style="color: var(--muted)">{{ $report->user?->email }}</small>
                            </td>
                            <td>
                                <span class="badge badge-{{ $report->status }}">{{ \App\Support\DisplayLabels::status($report->status) }}</span>
                            </td>
                            <td>
                                <form class="status-form" method="POST" action="{{ route('admin.reports.status', $report) }}">
                                    @csrf
                                    @method('PUT')
                                    <select name="status" onchange="this.form.submit()" aria-label="Changer le statut">
                                        <option value="pending" @selected($report->status === 'pending')>En attente</option>
                                        <option value="in_progress" @selected($report->status === 'in_progress')>En cours</option>
                                        <option value="resolved" @selected($report->status === 'resolved')>Résolu</option>
                                    </select>
                                </form>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="7" style="text-align:center; color: var(--muted); padding: 32px;">
                                Aucun signalement pour ce filtre.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>

            @if ($reports->hasPages())
                <div class="pagination">
                    @if ($reports->onFirstPage())
                        <span>← Précédent</span>
                    @else
                        <a href="{{ $reports->previousPageUrl() }}">← Précédent</a>
                    @endif

                    <span class="active">Page {{ $reports->currentPage() }} sur {{ $reports->lastPage() }}</span>

                    @if ($reports->hasMorePages())
                        <a href="{{ $reports->nextPageUrl() }}">Suivant →</a>
                    @else
                        <span>Suivant →</span>
                    @endif
                </div>
            @endif
        </div>
    </main>
</div>
@endsection
