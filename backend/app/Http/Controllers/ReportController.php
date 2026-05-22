<?php

namespace App\Http\Controllers;

use App\Models\Report;
use App\Services\FcmNotificationService;
use App\Services\GeminiVisionClassifier;
use App\Services\ReportClassifier;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class ReportController extends Controller
{
    public function __construct(
        private readonly ReportClassifier $classifier,
        private readonly GeminiVisionClassifier $gemini,
    ) {}

    public function index(Request $request): JsonResponse
    {
        $reports = Report::query()
            ->when($request->query('status'), fn ($q, $status) => $q->where('status', $status))
            ->latest()
            ->paginate((int) $request->query('per_page', 50));

        return response()->json($reports);
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'title' => ['required', 'string', 'max:255'],
            'description' => ['nullable', 'string'],
            'latitude' => ['required', 'numeric', 'between:-90,90'],
            'longitude' => ['required', 'numeric', 'between:-180,180'],
            'image' => ['nullable', 'image', 'max:5120'],
        ]);

        $imageFile = $request->file('image');
        $imagePath = null;
        $classificationSource = 'text_fallback';
        $geminiError = null;

        if ($imageFile) {
            $imagePath = $imageFile->store('reports', 'public');
            $vision = $this->gemini->classifyFromImage($imageFile);
            $geminiError = $vision['error'];

            if ($vision['category'] !== null) {
                $category = $vision['category'];
                $classificationSource = 'gemini_vision';
            } else {
                $category = $this->classifier->classify(
                    $validated['title'],
                    $validated['description'] ?? null
                );
            }
        } else {
            $category = $this->classifier->classify(
                $validated['title'],
                $validated['description'] ?? null
            );
        }

        $report = $request->user()->reports()->create([
            'title' => $validated['title'],
            'description' => $validated['description'] ?? null,
            'category' => $category,
            'latitude' => $validated['latitude'],
            'longitude' => $validated['longitude'],
            'image_path' => $imagePath,
            'status' => Report::STATUS_PENDING,
        ]);

        $aiPayload = [
            'category' => $category,
            'source' => $classificationSource,
            'message' => $classificationSource === 'gemini_vision'
                ? 'Catégorie déduite de l\'image via Gemini Vision.'
                : 'Catégorie déduite du titre et de la description (repli texte).',
        ];

        if (config('app.debug') && $geminiError !== null) {
            $aiPayload['gemini_error'] = $geminiError;
        }

        return response()->json([
            'report' => $report,
            'ai_classification' => $aiPayload,
        ], 201);
    }

    public function show(Report $report): JsonResponse
    {
        return response()->json($report);
    }

    public function updateStatus(Request $request, Report $report, FcmNotificationService $fcm): JsonResponse
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

        return response()->json($report);
    }

    public function update(Request $request, Report $report): JsonResponse
    {
        $this->authorizeReport($request, $report);

        $validated = $request->validate([
            'title' => ['sometimes', 'string', 'max:255'],
            'description' => ['nullable', 'string'],
            'latitude' => ['sometimes', 'numeric', 'between:-90,90'],
            'longitude' => ['sometimes', 'numeric', 'between:-180,180'],
            'status' => ['sometimes', Rule::in([
                Report::STATUS_PENDING,
                Report::STATUS_IN_PROGRESS,
                Report::STATUS_RESOLVED,
            ])],
            'image' => ['nullable', 'image', 'max:5120'],
        ]);

        if ($request->hasFile('image')) {
            $validated['image_path'] = $request->file('image')->store('reports', 'public');
        }

        unset($validated['image']);

        if (isset($validated['title']) || isset($validated['description'])) {
            $validated['category'] = $this->classifier->classify(
                $validated['title'] ?? $report->title,
                $validated['description'] ?? $report->description
            );
        }

        $report->update($validated);

        return response()->json($report->fresh());
    }

    public function destroy(Request $request, Report $report): JsonResponse
    {
        $this->authorizeReport($request, $report);
        $report->delete();

        return response()->json(null, 204);
    }

    private function authorizeReport(Request $request, Report $report): void
    {
        if ($report->user_id !== $request->user()->id) {
            abort(403, 'Accès refusé.');
        }
    }
}
