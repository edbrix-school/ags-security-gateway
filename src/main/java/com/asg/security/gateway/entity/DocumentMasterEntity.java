package com.asg.security.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "GLOBAL_DOC_MASTER")
public class DocumentMasterEntity {

    @Id
    @Column(name = "DOC_ID")
    private String docId;

    @Column(name = "DOC_TYPE")
    private String docType;

    /** Y = this document posts to General Ledger — financial and transaction period checks apply */
    @Column(name = "GL_POSTING")
    private String glPosting;

    /** Y = this document affects inventory — stock period check applies */
    @Column(name = "INVENTORY_DOCUMENT")
    private String inventoryDocument;

    public boolean isGlDocument() {
        return "Y".equalsIgnoreCase(glPosting);
    }

    public boolean isInventoryDocument() {
        return "Y".equalsIgnoreCase(inventoryDocument);
    }
}
