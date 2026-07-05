# Test Data - Federal Holidays API

This folder contains test files for the Federal Holidays API.

## 📁 Files

### **Federal-Holidays-API.postman_collection.json**
Complete Postman collection with 20 pre-configured requests.

**Quick Start:**
1. Open Postman
2. File → Import → Select this file
3. Run "1. Generate JWT Token" request
4. Token and Correlation ID auto-save to variables
5. Test all endpoints!

---

### **holidays.csv**
Sample CSV file for bulk upload testing (4 holidays).

**Format:**
```csv
holidayName,holidayDate,country,isRecurring,description
Memorial Day,2026-05-25,USA,true,Honors military personnel
...
```

**How to Use in Postman:**
1. Run "9a. Upload Holidays (CSV)" request
2. In Body tab, click **"Select Files"**
3. Navigate to this file: `federal-holidays-api/test-data/holidays.csv`
4. **DO NOT** manually add Content-Type header
5. Send request

**Expected Response:**
```json
{
  "totalRecords": 4,
  "successCount": 4,
  "errors": [],
  "failureCount": 0
}
```

---

### **holidays.json**
Sample JSON file for bulk upload testing (2 holidays).

**Format:**
```json
[
  {
    "holidayName": "Presidents Day",
    "holidayDate": "2026-02-16",
    "country": "USA",
    "isRecurring": true,
    "description": "Honors US presidents"
  }
]
```

**How to Use in Postman:**
1. Run "9b. Upload Holidays (JSON)" request
2. In Body tab, click **"Select Files"**
3. Navigate to this file: `federal-holidays-api/test-data/holidays.json`
4. **DO NOT** manually add Content-Type header
5. Send request

**Expected Response:**
```json
{
  "totalRecords": 2,
  "successCount": 2,
  "errors": [],
  "failureCount": 0
}
```

---

## ⚠️ Common Issues

### **500 Error: "Content-Type is not supported"**

**Cause:** Manually added Content-Type header or wrong Body type

**Fix:**
1. Body tab → Select **"form-data"** (not raw, not binary)
2. Remove any manual Content-Type header
3. Postman will auto-add: `Content-Type: multipart/form-data; boundary=...`

---

### **File Not Found**

**Cause:** Relative path in "src" field doesn't work

**Fix:**
1. Use Postman's **"Select Files"** button (don't type path)
2. Navigate to absolute path on your machine

---

### **409 Conflict - Duplicate Holiday**

**Cause:** Trying to upload holidays that already exist (same country + date)

**Fix:**
1. Delete existing holidays first (request "10. Delete Holiday")
2. Or modify test data to use different dates
3. Or restart application (clears H2 in-memory database)

---

## 🎯 Testing Workflow

**Standard flow:**
```
1. Generate JWT Token          (saves to {{token}} variable)
2. Create Holiday              (POST)
3. Get All Holidays            (GET)
4. Get Holiday by ID           (GET /:id)
5. Search by Country           (GET /search?country=USA)
6. Search by Year              (GET /search?year=2026)
7. Search by Date Range        (GET /search?startDate=...&endDate=...)
8. Get with Pagination         (GET ?offset=0&limit=5)
9. Update Holiday              (PUT /:id)
9a. Upload CSV                 (POST /upload with holidays.csv)
9b. Upload JSON                (POST /upload with holidays.json)
10. Delete Holiday             (DELETE /:id)

Error scenarios also included!
```

---

## 📝 Notes

- Max file size: **10MB**
- Supported formats: **CSV, JSON**
- CSV must have header row: `holidayName,holidayDate,country,isRecurring,description`
- JSON must be array of objects
- Partial success allowed - some records may fail, API returns error details
