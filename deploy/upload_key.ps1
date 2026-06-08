$pubkey = Get-Content "$env:USERPROFILE\.ssh\id_ed25519.pub"
$si = New-Object System.Diagnostics.ProcessStartInfo
$si.FileName = "ssh"
$si.Arguments = "-o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=15 -o PreferredAuthentications=password root@2604:abc0:103:415:2::45 `"mkdir -p ~/.ssh && echo `'$pubkey`' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys`""
$si.UseShellExecute = $false
$si.RedirectStandardInput = $true
$si.RedirectStandardOutput = $true
$si.RedirectStandardError = $true
$p = [System.Diagnostics.Process]::Start($si)
Start-Sleep -Milliseconds 3000
$p.StandardInput.WriteLine("bA9hfitSWkm3")
$p.StandardInput.Close()
$out = $p.StandardOutput.ReadToEnd()
$err = $p.StandardError.ReadToEnd()
$p.WaitForExit(25000)
Write-Host "EXIT: $($p.ExitCode)"
if ($out) { Write-Host "OUT: $out" }
if ($err) { Write-Host "ERR: $err" }
