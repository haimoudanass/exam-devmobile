<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\UserFcmController;
use App\Http\Controllers\ReportController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::prefix('v1')->group(function () {
    if (config('app.debug')) {
        Route::get('/debug/gemini-config', function () {
            $key = config('gemini.api_key');

            return response()->json([
                'key_loaded' => ! empty($key),
                'key_length' => $key ? strlen($key) : 0,
                'key_preview' => $key ? substr($key, 0, 8).'…'.substr($key, -4) : null,
                'model' => config('gemini.model'),
                'config_cached' => app()->configurationIsCached(),
                'env_file_exists' => file_exists(base_path('.env')),
            ]);
        });
    }

    Route::post('/auth/register', [AuthController::class, 'register']);
    Route::post('/auth/login', [AuthController::class, 'login']);

    Route::middleware('auth:sanctum')->group(function () {
        Route::get('/user', fn (Request $request) => $request->user());
        Route::post('/auth/logout', [AuthController::class, 'logout']);
        Route::put('/users/fcm-token', [UserFcmController::class, 'updateToken']);

        Route::put('/reports/{report}/status', [ReportController::class, 'updateStatus'])
            ->middleware('admin');
        Route::apiResource('reports', ReportController::class);
    });
});
