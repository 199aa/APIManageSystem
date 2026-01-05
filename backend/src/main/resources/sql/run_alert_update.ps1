# 执行告警数据更新脚本
# 请根据实际情况修改MySQL密码

Write-Host "正在执行告警数据库更新..." -ForegroundColor Green

# 提示输入密码
$password = Read-Host "请输入MySQL root密码" -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
$plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# 执行SQL文件
$sqlFile = "D:\ApiManageSystem\backend\src\main\resources\sql\alert.sql"
$content = Get-Content $sqlFile -Raw

# 使用管道方式执行
$content | mysql -u root -p"$plainPassword" api_manage

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ alert.sql 执行成功！" -ForegroundColor Green
} else {
    Write-Host "✗ alert.sql 执行失败！" -ForegroundColor Red
}

Write-Host "`n按任意键继续..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
