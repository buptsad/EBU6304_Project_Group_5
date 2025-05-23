# Define the path to the CSV file
$csvPath = "c:\Users\222\Documents\0_0\Java_learning\Mini_project_test\user_data\Bill\user_bill.csv"
$backupPath = "c:\Users\222\Documents\0_0\Java_learning\Mini_project_test\user_data\Bill\user_bill_backup.csv"

# Create a backup of the original file
Copy-Item -Path $csvPath -Destination $backupPath

# Define the categories to use
$categories = @("Gift", "Entertainment", "Service", "Shopping", "Other")

# Read the CSV file
$data = Import-Csv -Path $csvPath

# Modify each row
foreach ($row in $data) {
    # Replace the Category with a random selection
    $row.Category = $categories | Get-Random
}

# Write the modified data to a temporary CSV file
$tempPath = "$csvPath.temp"
$data | Export-Csv -Path $tempPath -NoTypeInformation

# Read the content, remove quotes, and write back to original file
$content = Get-Content -Path $tempPath
$content = $content -replace '"', ''
$content | Set-Content -Path $csvPath -Encoding UTF8

# Remove the temporary file
Remove-Item -Path $tempPath