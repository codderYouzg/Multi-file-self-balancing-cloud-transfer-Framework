package edu.youzg.resource_founder.core;

/**
 * 资源的基本信息<br/>
 * 服务器的名称、当前资源的id、当前资源的version
 */
public class ResourceBaseInfo {
    private String app; // 服务器的名称
    private String id;  // 当前资源的 id
    private String version; // 当前资源的 版本号

    public ResourceBaseInfo(String app, String id, String version) {
        this.app = app;
        this.id = id;
        this.version = version;
    }

    public String getApp() {
        return this.app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((app == null) ? 0 : app.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            ResourceBaseInfo other = (ResourceBaseInfo) obj;
            if (this.app == null) {
                if (other.app != null) {
                    return false;
                }
            } else if (!this.app.equals(other.app)) {
                return false;
            }

            if (this.id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!this.id.equals(other.id)) {
                return false;
            }

            if (this.version == null) {
                if (other.version != null) {
                    return false;
                }
            } else if (!this.version.equals(other.version)) {
                return false;
            }

            return true;
        }
    }

    @Override
    public String toString() {
        return "[" + this.app + "]" + this.id + ":" + this.version;
    }

}
