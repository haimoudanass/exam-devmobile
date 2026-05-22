<?php

namespace App\Services;

use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;

class GeminiVisionClassifier
{
    private const PROMPT = 'Analyze this image of a reported city issue. Categorize it strictly as exactly ONE of the following words: safety, lighting, waste, water, infrastructure, general. If the image depicts any form of danger, hazard, or risk, you must output safety. Output ONLY the category word.';

    private const ALLOWED = [
        'safety',
        'lighting',
        'waste',
        'water',
        'infrastructure',
        'general',
    ];

    /** @var string|null Last Gemini error (for debug responses in local). */
    public static ?string $lastError = null;

    /**
     * @return array{category: ?string, error: ?string}
     */
    public function classifyFromImage(UploadedFile $image): array
    {
        self::$lastError = null;

        $apiKey = config('gemini.api_key');
        if (empty($apiKey)) {
            self::$lastError = 'GEMINI_API_KEY is empty in config. Run php artisan config:clear and restart serve.';

            return ['category' => null, 'error' => self::$lastError];
        }

        $model = config('gemini.model', 'gemini-2.0-flash');
        $mimeType = $image->getMimeType() ?: 'image/jpeg';
        $base64 = base64_encode(file_get_contents($image->getRealPath()));

        $url = 'https://generativelanguage.googleapis.com/v1beta/models/'.$model.':generateContent';

        try {
            $http = Http::timeout(30)
                ->withHeaders([
                    'Content-Type' => 'application/json',
                    'X-goog-api-key' => $apiKey,
                ]);
            if (app()->environment('local')) {
                $http = $http->withoutVerifying();
            }

            $response = $http->post($url, [
                'contents' => [
                    [
                        'parts' => [
                            ['text' => self::PROMPT],
                            [
                                'inline_data' => [
                                    'mime_type' => $mimeType,
                                    'data' => $base64,
                                ],
                            ],
                        ],
                    ],
                ],
            ]);

            if (! $response->successful()) {
                self::$lastError = data_get($response->json(), 'error.message', 'HTTP '.$response->status());
                Log::warning('Gemini API error — using text fallback', [
                    'status' => $response->status(),
                    'message' => self::$lastError,
                    'model' => $model,
                ]);

                return ['category' => null, 'error' => self::$lastError];
            }

            $text = data_get($response->json(), 'candidates.0.content.parts.0.text');
            if (! is_string($text) || trim($text) === '') {
                self::$lastError = 'Gemini returned an empty category.';

                return ['category' => null, 'error' => self::$lastError];
            }

            return ['category' => $this->normalizeCategory($text), 'error' => null];
        } catch (\Throwable $e) {
            self::$lastError = $e->getMessage();
            Log::warning('Gemini vision classification failed', ['error' => self::$lastError]);

            return ['category' => null, 'error' => self::$lastError];
        }
    }

    private function normalizeCategory(string $raw): string
    {
        $word = Str::lower(trim($raw));
        $word = preg_replace('/[^a-z_]/', '', $word) ?? 'general';

        if (in_array($word, self::ALLOWED, true)) {
            return $word;
        }

        foreach (self::ALLOWED as $allowed) {
            if (str_contains($word, $allowed)) {
                return $allowed;
            }
        }

        return 'general';
    }
}
