<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class UserFcmController extends Controller
{
    public function updateToken(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'fcm_token' => ['required', 'string', 'max:512'],
        ]);

        $user = $request->user();
        $user->update(['fcm_token' => $validated['fcm_token']]);

        return response()->json([
            'message' => 'Jeton FCM enregistré.',
            'fcm_token' => $user->fcm_token,
        ]);
    }
}
