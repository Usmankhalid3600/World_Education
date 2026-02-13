# Topic Content with Binary Data - Quick Reference

## What Changed?

### Database
✅ Added `topic_content_data` LONGBLOB column to store complete files (up to 4GB)

### Entity
✅ `TopicContent.java` - Added `byte[] topicContentData` field with `@Lob` annotation

### DTO
✅ `TopicContentDTO.java` - Added:
- `contentDataBase64` (String) - Base64 encoded binary data for JSON response
- `contentSize` (Long) - File size in bytes
- `setTopicContentData(byte[])` - Automatically converts to Base64

### Service
✅ `TopicService.java` - Added 3 new methods:
1. `uploadTopicContent()` - Upload via MultipartFile
2. `uploadTopicContentBase64()` - Upload via Base64 JSON
3. `deleteTopicContent()` - Soft delete content

### Controller
✅ `TopicController.java` - Added 3 new endpoints:
1. `POST /api/topics/{topicId}/contents/upload` - Multipart upload
2. `POST /api/topics/{topicId}/contents/upload-base64` - JSON upload
3. `DELETE /api/topics/contents/{contentId}` - Delete content

## API Endpoints Summary

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/topics/{topicId}/contents` | Get all contents (with Base64 data) | ✅ JWT |
| POST | `/api/topics/{topicId}/contents/upload` | Upload file (multipart) | ✅ JWT |
| POST | `/api/topics/{topicId}/contents/upload-base64` | Upload file (Base64 JSON) | ✅ JWT |
| DELETE | `/api/topics/contents/{contentId}` | Delete content (soft) | ✅ JWT |

## Response Format

### GET /api/topics/1/contents
```json
{
  "success": true,
  "message": "Topic contents retrieved successfully",
  "data": {
    "topicId": 1,
    "topicName": "Addition",
    "hasAccess": true,
    "accessType": "TOPIC_SUBSCRIPTION",
    "contents": [
      {
        "contentId": 1,
        "fileName": "addition_basics.pdf",
        "fileType": "PDF",
        "contentDataBase64": "JVBERi0xLjQKJ...",  // 👈 Base64 encoded file
        "contentSize": 245678,                      // 👈 Size in bytes
        "uploadedAt": "2024-01-15T10:00:00"
      }
    ]
  }
}
```

## Upload Examples

### Option 1: Multipart Upload (Recommended for Web/Mobile)
```bash
curl -X POST \
  http://localhost:8080/api/topics/1/contents/upload \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -F 'file=@/path/to/document.pdf'
```

### Option 2: Base64 JSON Upload
```bash
curl -X POST \
  http://localhost:8080/api/topics/1/contents/upload-base64 \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "fileName": "document.pdf",
    "fileType": "PDF",
    "fileDataBase64": "JVBERi0xLjQKJ..."
  }'
```

## Client-Side: Download File from Base64

### JavaScript
```javascript
// 1. Fetch content
const response = await fetch('http://localhost:8080/api/topics/1/contents', {
  headers: { 'Authorization': 'Bearer ' + token }
});
const data = await response.json();

// 2. Get first content's Base64 data
const content = data.data.contents[0];
const base64Data = content.contentDataBase64;

// 3. Convert Base64 to Blob
const byteCharacters = atob(base64Data);
const byteNumbers = new Array(byteCharacters.length);
for (let i = 0; i < byteCharacters.length; i++) {
  byteNumbers[i] = byteCharacters.charCodeAt(i);
}
const byteArray = new Uint8Array(byteNumbers);
const blob = new Blob([byteArray], { type: 'application/pdf' });

// 4. Download file
const url = URL.createObjectURL(blob);
const a = document.createElement('a');
a.href = url;
a.download = content.fileName;
a.click();
URL.revokeObjectURL(url);
```

### Android (Java)
```java
// 1. Get Base64 string from API response
String base64Data = content.getContentDataBase64();

// 2. Decode to bytes
byte[] fileData = Base64.decode(base64Data, Base64.DEFAULT);

// 3. Save to file
File file = new File(context.getFilesDir(), content.getFileName());
FileOutputStream fos = new FileOutputStream(file);
fos.write(fileData);
fos.close();
```

### iOS (Swift)
```swift
// 1. Get Base64 string from API response
let base64Data = content.contentDataBase64

// 2. Decode to Data
guard let fileData = Data(base64Encoded: base64Data) else { return }

// 3. Save to file
let fileURL = FileManager.default.temporaryDirectory
    .appendingPathComponent(content.fileName)
try? fileData.write(to: fileURL)
```

## Storage Strategy

### Files < 1MB → Store in Database (BLOB)
- PDFs, documents, small images
- Better for transactional integrity
- Easier backup/restore

### Files > 1MB → Store Externally (S3/Azure)
- Videos, large PDFs, high-res images
- Better database performance
- Use `file_path_url` field
- Set `topic_content_data` to NULL

## Configuration

### application.properties
```properties
# Max upload size (adjust as needed)
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### MySQL (if needed)
```sql
-- Increase max packet size for large BLOBs
SET GLOBAL max_allowed_packet=67108864; -- 64MB
```

## Testing with Postman

### 1. Upload File
- Method: POST
- URL: `{{baseUrl}}/api/topics/1/contents/upload`
- Headers: `Authorization: Bearer {{token}}`
- Body: form-data
  - Key: `file` (Type: File)
  - Value: Select file from computer

### 2. Get Contents (includes Base64 data)
- Method: GET
- URL: `{{baseUrl}}/api/topics/1/contents`
- Headers: `Authorization: Bearer {{token}}`
- Response will contain `contentDataBase64` field

### 3. Decode Base64 to File
- Copy `contentDataBase64` value
- Visit: https://base64.guru/converter/decode/pdf
- Paste and download

## Security Notes

✅ JWT authentication required for all endpoints  
✅ Subscription validation enforced (topic OR subject)  
✅ File type validation on upload  
✅ Soft delete (preserves audit trail)  
✅ Uploaded by tracked (customerId)  

⚠️ Add these in production:
- File size limits (currently 50MB)
- Virus/malware scanning
- File type whitelist (block .exe, .sh, etc.)
- Rate limiting on uploads

## Files Created/Updated

### New Files
1. `TopicContentUploadRequest.java` - DTO for upload requests
2. `BINARY_DATA_GUIDE.md` - Comprehensive guide
3. `sample_topic_content_with_binary_data.sql` - SQL examples
4. `QUICK_REFERENCE.md` - This file

### Updated Files
1. `TopicContent.java` - Added `topic_content_data` field
2. `TopicContentDTO.java` - Added Base64 conversion
3. `TopicService.java` - Added upload/delete methods
4. `TopicController.java` - Added 3 new endpoints
5. `WorldEducation_Complete_API.postman_collection.json` - Added test requests

## Next Steps

1. **Test Upload:**
   ```bash
   # Login first to get token
   curl -X POST http://localhost:8080/api/auth/login \
     -H 'Content-Type: application/json' \
     -d '{"userId":"student001","password":"student123","deviceId":"test","deviceType":"WEB"}'
   
   # Upload file
   curl -X POST http://localhost:8080/api/topics/1/contents/upload \
     -H 'Authorization: Bearer YOUR_TOKEN' \
     -F 'file=@test.pdf'
   ```

2. **Retrieve and Decode:**
   - GET `/api/topics/1/contents`
   - Copy `contentDataBase64`
   - Decode using online tool or programmatically

3. **Frontend Integration:**
   - Implement file upload UI
   - Add download button for contents
   - Display PDFs/images inline

## Support

For detailed information, see:
- `BINARY_DATA_GUIDE.md` - Complete documentation
- `sample_topic_content_with_binary_data.sql` - SQL examples
- Postman collection - Test all endpoints
