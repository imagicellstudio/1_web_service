$date = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Write-Host "Starting sync at $date..."

# Add all changes
git add .

# Check if there are changes to commit
if ($(git status --porcelain)) {
    Write-Host "Changes detected. Committing..."
    git commit -m "Update: $date"
} else {
    Write-Host "No changes to commit."
}

# Push to remote
Write-Host "Pushing to remote..."
git push origin main

Write-Host "Sync completed successfully."
