@echo off
echo Starting In-Place Asset Optimization...

:: 1. Convert PNG to WebP in drawables (overwrite original)
echo Converting PNG to WebP...
for %%f in (app\src\main\res\drawable\*.png) do (
    magick "%%f" -quality 80 "%%~dpnf.webp"
    del "%%f"
)

:: 2. Convert JPG/JPEG to WebP in assets (overwrite original)
echo Converting JPG/JPEG to WebP in assets...
for /r "app\src\main\assets" %%f in (*.jpg *.jpeg) do (
    magick "%%f" -quality 80 "%%~dpnf.webp"
    del "%%f"
)

:: 3. Resize faces (overwrites file)
echo Resizing oversized faces...
for %%f in (app\src\main\res\drawable\*face*.webp) do (
    magick "%%f" -resize "512x512>" -quality 75 "%%f"
)

:: 4. Resize backgrounds (overwrites file)
echo Resizing oversized backgrounds...
for %%f in (app\src\main\res\drawable\*bg*.webp) do (
    magick "%%f" -resize "720x720>" -quality 75 "%%f"
)

echo Asset optimization complete!
pause