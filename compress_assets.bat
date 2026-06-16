@echo off
echo Starting Selective Asset Optimization...

:: 1. Convert all remaining PNGs to WebP (lossless) in drawables
echo Converting remaining PNGs to WebP...
for %%f in (app\src\main\res\drawable\*.png) do (
    magick "%%f" -quality 80 "%%~dpnf.webp"
    del "%%f"
)

:: 2. Recursive conversion: JPG/JPEG to WebP in assets
echo Converting JPG/JPEG to WebP in assets...
for /r "app\src\main\assets" %%f in (*.jpg *.jpeg) do (
    magick "%%f" -quality 80 "%%~dpnf.webp"
    del "%%f"
)

:: 3. Target only "face" files larger than 512x512
echo Resizing oversized faces...
for %%f in (app\src\main\res\drawable\*face*.webp) do (
    magick "%%f" -resize "512x512>" -quality 75 "%%f"
)

:: 4. Target only large backgrounds larger than 720x720
echo Resizing oversized backgrounds...
for %%f in (app\src\main\res\drawable\*bg*.webp) do (
    magick "%%f" -resize "720x720>" -quality 75 "%%f"
)

echo Asset optimization complete!
pause