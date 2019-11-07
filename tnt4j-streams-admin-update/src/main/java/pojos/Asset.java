
package pojos;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "url", "id", "node_id", "name", "label", "uploader", "content_type", "state", "size",
		"download_count", "created_at", "updated_at", "browser_download_url" })
public class Asset {

	@JsonProperty("url")
	private String url;
	@JsonProperty("id")
	private Integer id;
	@JsonProperty("node_id")
	private String nodeId;
	@JsonProperty("name")
	private String name;
	@JsonProperty("label")
	private Object label;
	@JsonProperty("uploader")
	private Uploader uploader;
	@JsonProperty("content_type")
	private String contentType;
	@JsonProperty("state")
	private String state;
	@JsonProperty("size")
	private Integer size;
	@JsonProperty("download_count")
	private Integer downloadCount;
	@JsonProperty("created_at")
	private String createdAt;
	@JsonProperty("updated_at")
	private String updatedAt;
	@JsonProperty("browser_download_url")
	private String browserDownloadUrl;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	@JsonProperty("url")
	public String getUrl() {
		return url;
	}

	@JsonProperty("url")
	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty("id")
	public Integer getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(Integer id) {
		this.id = id;
	}

	@JsonProperty("node_id")
	public String getNodeId() {
		return nodeId;
	}

	@JsonProperty("node_id")
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("label")
	public Object getLabel() {
		return label;
	}

	@JsonProperty("label")
	public void setLabel(Object label) {
		this.label = label;
	}

	@JsonProperty("uploader")
	public Uploader getUploader() {
		return uploader;
	}

	@JsonProperty("uploader")
	public void setUploader(Uploader uploader) {
		this.uploader = uploader;
	}

	@JsonProperty("content_type")
	public String getContentType() {
		return contentType;
	}

	@JsonProperty("content_type")
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@JsonProperty("state")
	public String getState() {
		return state;
	}

	@JsonProperty("state")
	public void setState(String state) {
		this.state = state;
	}

	@JsonProperty("size")
	public Integer getSize() {
		return size;
	}

	@JsonProperty("size")
	public void setSize(Integer size) {
		this.size = size;
	}

	@JsonProperty("download_count")
	public Integer getDownloadCount() {
		return downloadCount;
	}

	@JsonProperty("download_count")
	public void setDownloadCount(Integer downloadCount) {
		this.downloadCount = downloadCount;
	}

	@JsonProperty("created_at")
	public String getCreatedAt() {
		return createdAt;
	}

	@JsonProperty("created_at")
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@JsonProperty("updated_at")
	public String getUpdatedAt() {
		return updatedAt;
	}

	@JsonProperty("updated_at")
	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	@JsonProperty("browser_download_url")
	public String getBrowserDownloadUrl() {
		return browserDownloadUrl;
	}

	@JsonProperty("browser_download_url")
	public void setBrowserDownloadUrl(String browserDownloadUrl) {
		this.browserDownloadUrl = browserDownloadUrl;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
	}

}
