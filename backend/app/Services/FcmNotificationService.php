<?php

namespace App\Services;

use App\Models\Report;
use App\Models\User;
use Illuminate\Support\Facades\Log;
use Kreait\Firebase\Contract\Messaging;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;
use Throwable;

class FcmNotificationService
{
    public function sendReportResolved(User $user, Report $report): void
    {
        if (empty($user->fcm_token)) {
            return;
        }

        $credentialsPath = $this->credentialsPath();
        if (! is_file($credentialsPath)) {
            Log::info('FCM skipped: credentials file not found.', ['path' => $credentialsPath]);

            return;
        }

        try {
            $messaging = app(Messaging::class);
            $message = CloudMessage::withTarget('token', $user->fcm_token)
                ->withNotification(Notification::create(
                    'Signalement résolu',
                    "Votre signalement « {$report->title} » a été résolu !"
                ))
                ->withData([
                    'type' => 'report_resolved',
                    'report_id' => (string) $report->id,
                    'report_title' => $report->title,
                ]);

            $messaging->send($message);
        } catch (Throwable $e) {
            Log::warning('FCM send failed', [
                'user_id' => $user->id,
                'report_id' => $report->id,
                'error' => $e->getMessage(),
            ]);
        }
    }

    private function credentialsPath(): string
    {
        $credentials = config('firebase.projects.app.credentials')
            ?? env('FIREBASE_CREDENTIALS', storage_path('app/firebase-auth.json'));

        if (! is_string($credentials) || $credentials === '') {
            return storage_path('app/firebase-auth.json');
        }

        if (! str_starts_with($credentials, DIRECTORY_SEPARATOR) && ! preg_match('#^[A-Za-z]:[\\\\/]#', $credentials)) {
            return base_path($credentials);
        }

        return $credentials;
    }
}
