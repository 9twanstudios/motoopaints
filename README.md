# Motoopaint — Offline Automotive Paint & Livery Studio (Kenya)

Motoopaint is a 100% offline Android application for Kenya's automotive painters, panel beaters, colour technicians, and matatu/nganya customisation artists.

## Core Capabilities
- **Kenya-First Colour & Vehicle Catalogue**: Local Room database covering Toyota, Nissan, Isuzu, Subaru, Mazda, Honda, Mitsubishi, and Suzuki models, verified OEM codes with source provenance and honest gap flags, plus a standard colour alias dictionary.
- **Photo Painter & Car Photo Library**: Take vehicle photos directly in the bay (with runtime `CAMERA` permission checks) or import via Android's zero-permission Photo Picker, brush-mask body panels with undo/redo, simulate finishes, and compare with a Before/After split slider.
- **Custom Livery Studio (Nganya & Fleet)**: Side and front templates (Probox/Wagon, Saloon, Isuzu Pickup, Hiace/Matatu) with panel-by-panel recolouring, graphic stripes, motifs, route typography, and PNG presentation exports.
- **Jobs, Formulas & Spray-Outs**: Track vehicle jobs, technician-owned formulas, spray-out test outcomes, and KSh material costs.
- **Workshop Stock, Local Analytics & Backup**: Manage inventory with low-stock alerts, review on-device analytics, switch Daylight / Dark Booth themes, and export/restore JSON backups via Android's Storage Access Framework.

## GitHub Releases
1. Open AI Studio's **Settings** menu and choose **Push to GitHub** to sync the project to your repository (every push to `main`/`master` runs tests and builds downloadable APK artifacts).
2. Publish a GitHub Release in either of two ways:
   - **From GitHub UI**: Open **Actions -> Motoopaint Android Release -> Run workflow**, enter a `release_tag` (e.g. `v1.0.0`), and run.
   - **From Git CLI**: Push a version tag (`git tag v1.0.0 && git push origin v1.0.0`).
3. Optional: add repository secrets `KEYSTORE_BASE64`, `STORE_PASSWORD`, and `KEY_PASSWORD` to attach signed release APKs and AABs automatically.

## F-Droid Server & F-Droid Client
`.github/workflows/fdroid-repo.yml` uses `fdroidserver` to publish an F-Droid Client repository and direct APK download page on GitHub Pages:
1. After pushing to GitHub, open **Settings -> Pages** in your GitHub repository and set **Source** to **GitHub Actions**.
2. Wait for **Motoopaint F-Droid Server Repo** to finish, then open `https://<your-github-username>.github.io/<repo-name>/` on your phone to tap **Open in F-Droid Client** (or add `https://<your-github-username>.github.io/<repo-name>/repo` inside F-Droid -> Settings -> Repositories) or download the APK directly.
3. For persistent F-Droid repository signing across updates, add secrets `FDROID_KEYSTORE_BASE64`, `FDROID_KEYSTORE_PASS`, `FDROID_KEY_PASS`, and `FDROID_KEY_ALIAS`.



