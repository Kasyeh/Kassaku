#!/bin/bash

echo "=== Testing Backend API ==="
echo ""
echo "1. Testing endpoint riwayat untuk user ID 1:"
curl -X GET "http://localhost:8000/api/riwayat/1" -H "Accept: application/json"
echo ""
echo ""
echo "2. Testing dengan user ID yang Anda gunakan (ganti 1 dengan ID Anda):"
echo "   curl -X GET \"http://localhost:8000/api/riwayat/YOUR_USER_ID\" -H \"Accept: application/json\""
echo ""
