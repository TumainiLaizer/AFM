@echo off
echo Starting Recursive Asset Optimization (Skip Existing)...

set MAGICK_PATH="C:\Program Files\ImageMagick-7.1.2-Q8\magick.exe"

if not exist %MAGICK_PATH% (
    echo ImageMagick not found at %MAGICK_PATH%
    pause
    exit /b
)

:: 1. Convert PNG, JPG, JPEG to WebP recursively in assets if .webp doesn't exist
echo Converting images to WebP in assets (recursive, skip existing)...
for /r "app\src\main\assets" %%f in (*.webp *.jpg *.jpeg) do (
    if not exist "%%~dpnf.webp" (
        echo Converting: %%f
        %MAGICK_PATH% "%%f" -quality 80 "%%~dpnf.webp"
        if %errorlevel% equ 0 del "%%f"
    )
)

:: 2. Handle existing drawables (same logic)
echo Converting PNG to WebP in drawables...
for %%f in (app\src\main\res\drawable\*.png) do (
    if not exist "%%~dpnf.webp" (
        %MAGICK_PATH% "%%f" -quality 80 "%%~dpnf.webp"
        if %errorlevel% equ 0 del "%%f"
    )
)

:: 3. Resize faces (recursive)
echo Resizing oversized faces...
for /r "app\src\main\assets" %%f in (*face*.webp) do (
    echo Resizing: %%f
    %MAGICK_PATH% "%%f" -resize "512x512>" -quality 75 "%%f"
)

:: 4. Resize backgrounds (recursive)
echo Resizing oversized backgrounds...
for /r "app\src\main\assets" %%f in (*bg*.webp) do (
    echo Resizing: %%f
    %MAGICK_PATH% "%%f" -resize "720x720>" -quality 75 "%%f"
)

echo Asset optimization complete!
pause