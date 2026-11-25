# Motoopaint — Offline Automotive Paint & Livery Studio (Kenya)

Motoopaint is a 100% offline Android application for Kenya's automotive painters, panel beaters, colour technicians, and matatu/nganya customisation artists.

## Core Capabilities
- **Kenya-First Colour & Vehicle Catalogue**: Local Room database covering Toyota, Nissan, Isuzu, Subaru, Mazda, Honda, Mitsubishi, and Suzuki models, verified OEM codes with source provenance and honest gap flags, plus a standard colour alias dictionary.
- **Photo Painter & Car Photo Library**: Take vehicle photos directly in the bay (with runtime `CAMERA` permission checks) or import via Android's zero-permission Photo Picker, brush-mask body panels with undo/redo, simulate finishes, and compare with a Before/After split slider.
- **Custom Livery Studio (Nganya & Fleet)**: Side and front templates (Probox/Wagon, Saloon, Isuzu Pickup, Hiace/Matatu) with panel-by-panel recolouring, graphic stripes, motifs, route typography, and PNG presentation exports.
- **Jobs, Formulas & Spray-Outs**: Track vehicle jobs, technician-owned formulas, spray-out test outcomes, and KSh material costs.
- **Workshop Stock, Local Analytics & Backup**: Manage inventory with low-stock alerts, review on-device analytics, switch Daylight / Dark Booth themes, and export/restore JSON backups via Android's Storage Access Framework.

## GitHub Releases
1. Use AI Studio's **Settings -> Push to GitHub** (or `git push`) to push the codebase to your GitHub repository.
2. Push a version tag (e.g. `git tag v1.0.0 && git push origin v1.0.0`) or create a tag in GitHub Releases to trigger `.github/workflows/android-release.yml`, which runs `:app:testDebugUnitTest`, builds APKs/AABs, and publishes a GitHub Release automatically.
3. Optional: to publish signed release APKs/AABs in CI, add repository secrets `KEYSTORE_BASE64`, `STORE_PASSWORD`, and `KEY_PASSWORD` (otherwise a debug APK is attached automatically).

