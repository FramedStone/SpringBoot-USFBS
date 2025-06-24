import { useState, useEffect } from "react";
import "@styles/MediaUpload.css";

const MediaUpload = ({
  initialFileCid,
  initialFileType,
  onFileChange,
  disabled = false,
}) => {
  const [currentMediaUrl, setCurrentMediaUrl] = useState(null);
  const [mediaType, setMediaType] = useState(initialFileType || null);
  const [file, setFile] = useState(null);
  const [filePreviewUrl, setFilePreviewUrl] = useState(null);
  const [fileType, setFileType] = useState(null);
  const [zoomedImage, setZoomedImage] = useState(null);

  // Load current media if editing
  useEffect(() => {
    if (initialFileCid) {
      const gatewayUrl = `https://gateway.pinata.cloud/ipfs/${initialFileCid}`;
      setCurrentMediaUrl(gatewayUrl);
      fetch(gatewayUrl, { method: "HEAD" })
        .then((res) => {
          const type = res.headers.get("content-type");
          if (type?.startsWith("image/")) setMediaType("image");
          else setMediaType("unsupported");
        })
        .catch(() => setMediaType("unsupported"));
    }
  }, [initialFileCid]);

  useEffect(() => {
    return () => {
      if (filePreviewUrl) URL.revokeObjectURL(filePreviewUrl);
    };
  }, [filePreviewUrl]);

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    setFile(selectedFile);
    onFileChange(selectedFile);

    if (filePreviewUrl) {
      URL.revokeObjectURL(filePreviewUrl);
      setFilePreviewUrl(null);
      setFileType(null);
    }

    if (selectedFile) {
      const type = selectedFile.type;
      if (type.startsWith("image/")) {
        setFileType("image");
        setFilePreviewUrl(URL.createObjectURL(selectedFile));
      } else {
        setFileType("unsupported");
        setFilePreviewUrl(null);
      }
    }
  };

  const handleZoom = (url, alt) => {
    setZoomedImage({ url, alt });
  };

  const closeZoom = () => setZoomedImage(null);

  const renderCurrentMedia = () => {
    if (!currentMediaUrl) return null;
    if (mediaType === "image") {
      return (
        <div className="current-media-preview">
          <img
            src={currentMediaUrl}
            alt="Current announcement media"
            className="media-preview-img"
            style={{ cursor: "zoom-in" }}
            onClick={() =>
              handleZoom(currentMediaUrl, "Current announcement media")
            }
          />
          <div className="media-preview-label">Current image file</div>
        </div>
      );
    }
    return (
      <div className="current-media-preview">
        <div className="media-preview-doc">Unsupported file type</div>
        <a
          href={currentMediaUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="media-preview-link"
        >
          Download Current File
        </a>
      </div>
    );
  };

  const renderNewFilePreview = () => {
    if (!file) return null;
    if (fileType === "image" && filePreviewUrl) {
      return (
        <div className="new-file-preview">
          <img
            src={filePreviewUrl}
            alt="New file preview"
            className="media-preview-img"
            style={{ cursor: "zoom-in" }}
            onClick={() => handleZoom(filePreviewUrl, "New file preview")}
          />
          <div className="media-preview-label">
            {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)
          </div>
        </div>
      );
    }
    if (fileType === "unsupported") {
      return (
        <div className="new-file-preview">
          <div className="media-preview-doc">
            Unsupported file type (only images allowed)
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div>
      {renderCurrentMedia()}
      <div className="form-group">
        <input
          type="file"
          onChange={handleFileChange}
          accept="image/*"
          disabled={disabled}
        />
        <small className="form-hint">
          Accepted: JPG, JPEG, PNG, GIF, BMP, SVG
        </small>
      </div>
      {renderNewFilePreview()}

      {zoomedImage && (
        <div className="media-zoom-overlay" onClick={closeZoom}>
          <div className="media-zoom-modal" onClick={(e) => e.stopPropagation()}>
            <button
              className="media-zoom-close"
              onClick={closeZoom}
              title="Close"
            >
              ×
            </button>
            <img
              src={zoomedImage.url}
              alt={zoomedImage.alt}
              className="media-zoom-img"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default MediaUpload;