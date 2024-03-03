#!/bin/bash

# 设置变量
VERSION_URL="http://limit.api.yyxcloud.com//gateMachine/queryVersion/4jyim/1"
INSTALL_DIR="/path/to/installation"
BACKUP_DIR="/path/to/backup"

# 获取版本信息
response=$(curl -s "$VERSION_URL")

# 解析JSON响应
code=$(echo "$response" | jq -r '.code')
if [ "$code" -eq 1 ]; then
    versionCode=$(echo "$response" | jq -r '.data.versionCode')
    downloadUrl=$(echo "$response" | jq -r '.data.downloadUrl')

    echo "发现新版本: $versionCode"

    # 下载更新包
    echo "下载更新包..."
    wget "$downloadUrl" -O latest.tar.gz

    # 备份原有程序
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
    fi
    cp -r "$INSTALL_DIR" "$BACKUP_DIR"

    # 解压更新包
    echo "安装新版本..."
    tar -zxvf latest.tar.gz -C "$INSTALL_DIR" --strip-components=1

    # 清理临时文件
    rm latest.tar.gz

    echo "更新完成"
else
    echo "未发现更新"
fi
