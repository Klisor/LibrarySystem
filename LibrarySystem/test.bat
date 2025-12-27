# ==================== 负载均衡测试脚本 ====================
Write-Host "=== 微服务负载均衡测试 ===" -ForegroundColor Cyan
Write-Host "观察请求如何分配到不同端口的实例" -ForegroundColor Yellow

$serviceToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJpZCI6NiwidXNlcm5hbWUiOiJhZG1pbiIsInN1YiI6ImFkbWluIiwiaWF0IjoxNzY2MjM4NDg0LCJleHAiOjE4NTI2Mzg0ODR9.ARaGr0dOujE3vd6t5eJXCcckGFrOb3l6jAEVmIRcN4Y"

# 1. 重新构建并启动所有服务
Write-Host "`n1. 重新构建并启动所有服务..." -ForegroundColor Green
docker-compose down
docker-compose up -d --build

Write-Host "等待服务启动（约40秒）..." -ForegroundColor Yellow
Start-Sleep -Seconds 40

# 2. 检查所有服务状态
Write-Host "`n2. 检查服务状态和端口..." -ForegroundColor Green
docker-compose ps | ForEach-Object {
    if ($_ -match "user-service|book-service|borrow-service") {
        Write-Host $_ -ForegroundColor White
    }
}

# 3. 检查Nacos注册情况
Write-Host "`n3. 访问Nacos控制台确认注册:" -ForegroundColor Green
Write-Host "   地址: http://localhost:8848/nacos" -ForegroundColor White
Write-Host "   应看到: user-service(3个实例), book-service(3个实例)" -ForegroundColor White

# 4. 发送负载均衡测试请求
Write-Host "`n4. 发送30次借书请求测试负载均衡..." -ForegroundColor Cyan
Write-Host "   用户服务端口: 8083, 8084, 8085" -ForegroundColor White
Write-Host "   图书服务端口: 8091, 8092, 8093" -ForegroundColor White

$successCount = 0
$totalRequests = 30

1..$totalRequests | ForEach-Object {
    $userId = ($_ % 5) + 1  # 用户ID 1-5循环
    $bookId = ($_ % 3) + 1  # 图书ID 1-3循环

    Write-Host "请求 #$_ (用户:$userId, 图书:$bookId)..." -NoNewline
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8082/api/borrow" -Method POST `
            -Headers @{
                "Authorization" = $serviceToken
                "Content-Type" = "application/json"
            } `
            -Body ("{'userId': $userId, 'bookId': $bookId}" -replace "'", '"') `
            -UseBasicParsing -ErrorAction Stop

        Write-Host " -> 成功" -ForegroundColor Green
        $successCount++
    } catch {
        Write-Host " -> 失败" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 300  # 适当间隔
}

Write-Host "`n负载均衡测试完成。成功: $successCount/$totalRequests 次" -ForegroundColor $(if ($successCount -gt 0) { "Green" } else { "Red" })

# 5. 查看每个实例的处理情况
Write-Host "`n5. 查看用户服务实例处理统计:" -ForegroundColor Cyan

# 用户服务实例统计
$userInstances = @(
    @{Name="user-service-1"; Port=8083},
    @{Name="user-service-2"; Port=8084},
    @{Name="user-service-3"; Port=8085}
)

Write-Host "`n用户服务负载分布:" -ForegroundColor Yellow
$userTotal = 0
foreach ($instance in $userInstances) {
    $logCount = (docker-compose logs $instance.Name --tail=100 | Select-String "负载均衡").Count
    $userTotal += $logCount
    Write-Host "  $($instance.Name) (端口:$($instance.Port)) : 处理了 $logCount 次请求" -ForegroundColor $(if ($logCount -gt 0) { "Green" } else { "Yellow" })
}
Write-Host "  总计: $userTotal 次" -ForegroundColor Cyan

# 图书服务实例统计
$bookInstances = @(
    @{Name="book-service-1"; Port=8091},
    @{Name="book-service-2"; Port=8092},
    @{Name="book-service-3"; Port=8093}
)

Write-Host "`n图书服务负载分布:" -ForegroundColor Yellow
$bookTotal = 0
foreach ($instance in $bookInstances) {
    $logCount = (docker-compose logs $instance.Name --tail=100 | Select-String "负载均衡").Count
    $bookTotal += $logCount
    Write-Host "  $($instance.Name) (端口:$($instance.Port)) : 处理了 $logCount 次请求" -ForegroundColor $(if ($logCount -gt 0) { "Green" } else { "Yellow" })
}
Write-Host "  总计: $bookTotal 次" -ForegroundColor Cyan

# 6. 查看示例日志
Write-Host "`n6. 查看示例日志（显示端口信息）:" -ForegroundColor Cyan

foreach ($instance in $userInstances) {
    $sampleLogs = docker-compose logs $instance.Name --tail=3 | Select-String "端口"
    if ($sampleLogs) {
        Write-Host "  $($instance.Name) 示例:" -ForegroundColor White
        $sampleLogs | ForEach-Object {
            Write-Host "    $($_.Line)" -ForegroundColor Gray
        }
    }
}

# 7. 熔断降级测试
Write-Host "`n7. 熔断降级测试..." -ForegroundColor Yellow

$confirm = Read-Host "是否执行熔断测试？(停止用户服务) 输入 y 继续"
if ($confirm -eq 'y') {
    Write-Host "停止所有用户服务实例..." -ForegroundColor Red
    docker-compose stop user-service-1 user-service-2 user-service-3
    Start-Sleep -Seconds 10

    Write-Host "发送5次请求（应触发Fallback）..." -ForegroundColor Yellow
    1..5 | ForEach-Object {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8082/api/borrow" -Method POST `
                -Headers @{
                    "Authorization" = $serviceToken
                    "Content-Type" = "application/json"
                } `
                -Body '{"userId": 1, "bookId": 1}' `
                -UseBasicParsing
            Write-Host "  请求 $_: 状态码 $($response.StatusCode)" -ForegroundColor Gray
        } catch {
            Write-Host "  请求 $_: 异常" -ForegroundColor Gray
        }
        Start-Sleep -Seconds 1
    }

    Write-Host "`n检查Fallback日志..." -ForegroundColor Cyan
    $fallbackLogs = docker-compose logs borrow-service --tail=50 | Select-String "Fallback|降级|fallback|服务不可用"
    if ($fallbackLogs) {
        Write-Host "✅ 熔断降级已触发！" -ForegroundColor Green
        $fallbackLogs | Select-Object -Last 3 | ForEach-Object {
            Write-Host "  [Fallback] $($_.Line)" -ForegroundColor Magenta
        }
    }

    Write-Host "`n重启用户服务..." -ForegroundColor Green
    docker-compose start user-service-1 user-service-2 user-service-3

    Write-Host "等待30秒恢复..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30

    Write-Host "发送恢复测试请求..." -ForegroundColor Green
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8082/api/borrow" -Method POST `
            -Headers @{
                "Authorization" = $serviceToken
                "Content-Type" = "application/json"
            } `
            -Body '{"userId": 1, "bookId": 1}' `
            -UseBasicParsing
        Write-Host "✅ 恢复成功: 状态码 $($response.StatusCode)" -ForegroundColor Green
    } catch {
        Write-Host "❌ 恢复失败" -ForegroundColor Red
    }
}

# 8. 最终验证
Write-Host "`n=== 测试完成 ===" -ForegroundColor Cyan
Write-Host "负载均衡结果:" -ForegroundColor White
Write-Host "  用户服务: 3个实例应该都处理了请求" -ForegroundColor White
Write-Host "  图书服务: 3个实例应该都处理了请求" -ForegroundColor White
Write-Host "  如果负载均衡工作正常，每个实例的处理次数应该大致相等" -ForegroundColor White

Write-Host "`n查看当前借阅记录验证功能:" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8082/api/borrow" -Method GET `
        -Headers @{ "Authorization" = $serviceToken } `
        -UseBasicParsing
    Write-Host "  当前借阅记录数量: $($response.Content | ConvertFrom-Json).data.length" -ForegroundColor Green
} catch {
    Write-Host "  查看借阅记录失败" -ForegroundColor Yellow
}