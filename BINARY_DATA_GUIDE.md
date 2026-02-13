# Topic Content Binary Data Guide

## Overview
The `topic_contents` table now supports storing complete files as binary data (BLOB) in the `topic_content_data` column. This allows you to store PDFs, images (JPG, PNG), videos, or any other file type directly in the database.

## Database Schema
```sql
Table: topic_contents
- content_id (PK)
- topic_id (FK references ed_topics.topic_id)
- file_name (VARCHAR)
- file_path_url (VARCHAR) - Optional: for external file storage
- file_type (VARCHAR) - PDF, JPG, PNG, MP4, etc.
- topic_content_data (LONGBLOB) - Binary file data (up to 4GB)
- uploaded_by (FK references users.customer_id)
- uploaded_at (DATETIME)
- is_active (BOOLEAN)
```

## Storage Options

### Option 1: Store Binary Data in Database (BLOB)
**Best for:** Files < 1MB (thumbnails, small PDFs, documents)

**Pros:**
- Single source of truth (all data in DB)
- ACID compliance and transactional integrity
- Easy backup/restore (entire database)
- No external dependencies

**Cons:**
- Can slow down database backups
- Larger database size
- Performance impact for very large files

### Option 2: Store Files Externally (S3, Azure Blob, File System)
**Best for:** Files > 1MB (videos, large PDFs, high-res images)

**Pros:**
- Better database performance
- Easier CDN integration
- Scalable storage
- Cost-effective for large files

**Cons:**
- Additional infrastructure complexity
- Sync issues between DB and file storage
- Need separate backup strategy

### Hybrid Approach (Recommended)
- Files < 1MB: Store as BLOB in database
- Files > 1MB: Store externally, save URL in `file_path_url`
- Set `topic_content_data` to NULL when using external storage

## API Endpoints

### 1. Get Topic Contents (with Binary Data)
```http
GET /api/topics/{topicId}/contents
Authorization: Bearer {jwt_token}
```

**Response:**
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
        "contentDataBase64": "JVBERi0xLjQKJeLjz9MKMyAwIG9iaiA8PC9UeXBlL...",
        "contentSize": 245678,
        "uploadedAt": "2024-01-15T10:00:00"
      }
    ],
    "totalContents": 1
  }
}
```

**Key Points:**
- Binary data is automatically converted to Base64 for JSON transmission
- `contentDataBase64`: Base64 encoded file data (can be decoded on client)
- `contentSize`: Size in bytes (helpful for download progress)

### 2. Upload File (Multipart Form Data)
```http
POST /api/topics/{topicId}/contents/upload
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data

Form Data:
- file: (binary file)
```

**Example using cURL:**
```bash
curl -X POST \
  http://localhost:8080/api/topics/1/contents/upload \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -F 'file=@/path/to/document.pdf'
```

**Example using JavaScript (fetch):**
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

fetch('http://localhost:8080/api/topics/1/contents/upload', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token
  },
  body: formData
})
.then(response => response.json())
.then(data => console.log('Uploaded:', data));
```

**Response:**
```json
{
  "success": true,
  "message": "File 'document.pdf' uploaded successfully. Content ID: 10, Size: 245678 bytes",
  "data": "10"
}
```

### 3. Upload File (Base64 JSON)
```http
POST /api/topics/{topicId}/contents/upload-base64
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "fileName": "document.pdf",
  "fileType": "PDF",
  "fileDataBase64": "JVBERi0xLjQKJeLjz9MKMyAwIG9iaiA8PC9UeXBlL..."
}
```

**Example using JavaScript:**
```javascript
// Convert file to Base64
function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const base64 = reader.result.split(',')[1]; // Remove data:mime;base64, prefix
      resolve(base64);
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

// Upload
const base64Data = await fileToBase64(fileInput.files[0]);

fetch('http://localhost:8080/api/topics/1/contents/upload-base64', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    fileName: 'document.pdf',
    fileType: 'PDF',
    fileDataBase64: base64Data
  })
})
.then(response => response.json())
.then(data => console.log('Uploaded:', data));
```

### 4. Delete Content
```http
DELETE /api/topics/contents/{contentId}
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "success": true,
  "message": "Content deleted successfully",
  "data": null
}
```

## Client-Side: Decoding Base64 to File

### JavaScript (Browser)
```javascript
// Decode Base64 to Blob
function base64ToBlob(base64, mimeType) {
  const byteCharacters = atob(base64);
  const byteNumbers = new Array(byteCharacters.length);
  
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  
  const byteArray = new Uint8Array(byteNumbers);
  return new Blob([byteArray], { type: mimeType });
}

// Download file
function downloadFile(base64Data, fileName, mimeType) {
  const blob = base64ToBlob(base64Data, mimeType);
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

// Display PDF in iframe
function displayPDF(base64Data) {
  const blob = base64ToBlob(base64Data, 'application/pdf');
  const url = URL.createObjectURL(blob);
  document.getElementById('pdfViewer').src = url;
}

// Display image
function displayImage(base64Data, mimeType) {
  const blob = base64ToBlob(base64Data, mimeType);
  const url = URL.createObjectURL(blob);
  document.getElementById('imagePreview').src = url;
}
```

### Android (Java/Kotlin)
```java
// Decode Base64 to byte array
byte[] fileData = Base64.decode(base64String, Base64.DEFAULT);

// Save to file
File file = new File(context.getFilesDir(), "document.pdf");
FileOutputStream fos = new FileOutputStream(file);
fos.write(fileData);
fos.close();

// Display image
Bitmap bitmap = BitmapFactory.decodeByteArray(fileData, 0, fileData.length);
imageView.setImageBitmap(bitmap);
```

### iOS (Swift)
```swift
// Decode Base64 to Data
guard let fileData = Data(base64Encoded: base64String) else { return }

// Save to file
let fileURL = FileManager.default.temporaryDirectory.appendingPathComponent("document.pdf")
try? fileData.write(to: fileURL)

// Display image
let image = UIImage(data: fileData)
imageView.image = image
```

## Postman Testing

### Upload File (Multipart)
1. Create new request: `POST /api/topics/1/contents/upload`
2. Headers:
   - `Authorization: Bearer {{token}}`
3. Body → form-data:
   - Key: `file` (Type: File)
   - Value: Select file from computer

### Upload File (Base64)
1. Create new request: `POST /api/topics/1/contents/upload-base64`
2. Headers:
   - `Authorization: Bearer {{token}}`
   - `Content-Type: application/json`
3. Body → raw (JSON):
```json
{
  "fileName": "test.pdf",
  "fileType": "PDF",
  "fileDataBase64": "JVBERi0xLjQKJeLjz9MKMyAwIG9iaiA8PC9UeXBlL..."
}
```

### Get Contents (Download)
1. Request: `GET /api/topics/1/contents`
2. Response will contain Base64 data in `contentDataBase64` field
3. Use online tool to decode: https://base64.guru/converter/decode/pdf
4. Or save response and decode programmatically

## File Size Limits

### MySQL BLOB Types
- `TINYBLOB`: 255 bytes
- `BLOB`: 64 KB
- `MEDIUMBLOB`: 16 MB
- `LONGBLOB`: 4 GB (Currently used)

### Spring Boot Configuration
Add to `application.properties`:
```properties
# Maximum file upload size
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

## Security Considerations

1. **File Type Validation**
   - Always validate `file_type` on server
   - Check file magic numbers (not just extension)
   - Reject executable files (.exe, .sh, .bat)

2. **File Size Limits**
   - Enforce max size (e.g., 50MB)
   - Prevent DoS attacks via large uploads

3. **Virus Scanning**
   - Integrate antivirus scanning before storage
   - Use ClamAV or similar

4. **Access Control**
   - Already implemented: subscription validation
   - Users can only download if they have subscription

5. **Encryption**
   - Consider encrypting sensitive files
   - Use database encryption at rest

## Performance Optimization

### 1. Pagination for Large Lists
```java
// Add pagination to repository
Page<TopicContent> findByTopicIdAndIsActiveTrue(Long topicId, Pageable pageable);
```

### 2. Exclude BLOB from List Queries
```java
// Use projection to avoid loading large BLOBs
@Query("SELECT new TopicContentDTO(c.contentId, c.fileName, c.fileType, c.uploadedAt) " +
       "FROM TopicContent c WHERE c.topicId = :topicId AND c.isActive = true")
List<TopicContentDTO> findByTopicIdWithoutBlob(@Param("topicId") Long topicId);
```

### 3. Stream Large Files
```java
// For files > 10MB, use streaming instead of loading in memory
@Transactional(readOnly = true)
public void streamContent(Long contentId, HttpServletResponse response) {
    TopicContent content = repository.findById(contentId).orElseThrow();
    response.setContentType("application/" + content.getFileType().toLowerCase());
    response.setHeader("Content-Disposition", "attachment; filename=" + content.getFileName());
    
    try (OutputStream out = response.getOutputStream()) {
        out.write(content.getTopicContentData());
        out.flush();
    }
}
```

## Troubleshooting

### Issue: "Packet too large" MySQL error
**Solution:** Increase `max_allowed_packet` in MySQL:
```sql
SET GLOBAL max_allowed_packet=67108864; -- 64MB
```
Or in `my.cnf`:
```ini
[mysqld]
max_allowed_packet=64M
```

### Issue: Out of Memory when uploading large files
**Solution:** Use streaming instead of loading entire file:
```java
@PostMapping("/upload-stream")
public void uploadStream(@RequestParam("file") MultipartFile file) throws IOException {
    try (InputStream is = file.getInputStream()) {
        // Process in chunks
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            // Process buffer
        }
    }
}
```

### Issue: Base64 decoding fails on client
**Solution:** Ensure no line breaks or whitespace in Base64 string:
```java
String cleanBase64 = base64String.replaceAll("\\s+", "");
```

## Example: Complete Upload Flow

### Backend (Already Implemented)
1. Client uploads file → `POST /api/topics/1/contents/upload`
2. Server validates authentication (JWT)
3. Server validates topic exists
4. Server reads file bytes
5. Server saves to database with BLOB data
6. Server returns success with content ID

### Frontend (Example)
```html
<!DOCTYPE html>
<html>
<head>
    <title>Upload Topic Content</title>
</head>
<body>
    <h1>Upload File</h1>
    <input type="file" id="fileInput" />
    <button onclick="uploadFile()">Upload</button>
    
    <script>
        const token = 'YOUR_JWT_TOKEN'; // From login response
        
        async function uploadFile() {
            const fileInput = document.getElementById('fileInput');
            const file = fileInput.files[0];
            
            if (!file) {
                alert('Please select a file');
                return;
            }
            
            const formData = new FormData();
            formData.append('file', file);
            
            try {
                const response = await fetch('http://localhost:8080/api/topics/1/contents/upload', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    },
                    body: formData
                });
                
                const result = await response.json();
                
                if (result.success) {
                    alert('File uploaded: ' + result.message);
                } else {
                    alert('Upload failed: ' + result.message);
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Upload error: ' + error.message);
            }
        }
    </script>
</body>
</html>
```

## Summary
✅ Binary data stored in `topic_content_data` LONGBLOB field  
✅ Automatic Base64 encoding for JSON responses  
✅ Two upload methods: Multipart and Base64  
✅ Subscription-based access control  
✅ Soft delete support  
✅ Supports all file types (PDF, JPG, PNG, MP4, etc.)  
✅ Production-ready with security and validation
