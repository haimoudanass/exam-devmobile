<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Report;
use App\Services\FcmNotificationService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;
use Illuminate\View\View;

class AdminDashboardController extends Controller
{
    public function index(Request $request): View
    {
        $statusFilter = $request->query('status');

        $reports = Report::query()
            ->with('user:id,name,email')
            ->when($statusFilter, fn ($q) => $q->where('status', $statusFilter))
            ->latest()
            ->paginate(20)
            ->withQueryString();

        $stats = [
            'total' => Report::count(),
            'pending' => Report::where('status', Report::STATUS_PENDING)->count(),
            'in_progress' => Report::where('status', Report::STATUS_IN_PROGRESS)->count(),
            'resolved' => Report::where('status', Report::STATUS_RESOLVED)->count(),
        ];

        return view('admin.dashboard', compact('reports', 'stats', 'statusFilter'));
    }

    public function updateStatus(Request $request, Report $report, FcmNotificationService $fcm): RedirectResponse
    {
        $validated = $request->validate([
            'status' => ['required', Rule::in([
                Report::STATUS_PENDING,
                Report::STATUS_IN_PROGRESS,
                Report::STATUS_RESOLVED,
            ])],
        ]);

        $previousStatus = $report->status;
        $report->update(['status' => $validated['status']]);
        $report->refresh();

        if (
            $previousStatus !== Report::STATUS_RESOLVED
            && $report->status === Report::STATUS_RESOLVED
        ) {
            $report->load('user');
            $fcm->sendReportResolved($report->user, $report);
        }

        return back()->with('success', 'Statut mis à jour — notification envoyée si résolu.');
    }
}
