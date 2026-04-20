package com.abelium.inatrace.db.entities.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serializable;

@Entity
public class ProductLabelCompanyDocument implements Serializable {

    @Id
    private Long productLabelId;

    @Id
    private Long companyDocumentId;

    public Long getProductLabelId() {
        return productLabelId;
    }

    public void setProductLabelId(Long productLabelId) {
        this.productLabelId = productLabelId;
    }

    public Long getCompanyDocumentId() {
        return companyDocumentId;
    }

    public void setCompanyDocumentId(Long companyDocumentId) {
        this.companyDocumentId = companyDocumentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductLabelCompanyDocument that = (ProductLabelCompanyDocument) o;
        if (productLabelId != null ? !productLabelId.equals(that.productLabelId) : that.productLabelId != null) return false;
        return companyDocumentId != null ? companyDocumentId.equals(that.companyDocumentId) : that.companyDocumentId == null;
    }

    @Override
    public int hashCode() {
        int result = productLabelId != null ? productLabelId.hashCode() : 0;
        result = 31 * result + (companyDocumentId != null ? companyDocumentId.hashCode() : 0);
        return result;
    }
}
