DROP TABLE T_DOC_ENCODING CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_DOC_ENCODING;





CREATE TABLE T_DOC_ENCODING (
       ENC_ID               NUMBER NOT NULL,
       ENC_NAME             VARCHAR2(16) NOT NULL,
       PRIMARY KEY (ENC_ID)
);

CREATE SEQUENCE SEQ_DOC_ENCODING INCREMENT BY 1 START WITH 1;
    
   




CREATE TABLE T_DOC_CONTENT (
       DC_ID                NUMBER NOT NULL,
       DC_ENCODING_ID       NUMBER NULL,
       DC_CONTENT           BLOB NULL,
       PRIMARY KEY (DC_ID), 
       FOREIGN KEY (DC_ENCODING_ID)
                             REFERENCES T_DOC_ENCODING
);


CREATE TABLE T_GROUP (
       GRP_ID               NUMBER NOT NULL,
       GRP_NAME             VARCHAR2(128) NULL,
       PRIMARY KEY (GRP_ID)
);

DROP TABLE T_USER CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_USER;





CREATE TABLE T_USER (
       USR_ID               NUMBER NOT NULL,
       USR_LOGIN            VARCHAR2(16) NOT NULL,
       USR_GROUP_ID         NUMBER NULL,
       USR_PASS             VARCHAR2(16) NOT NULL,
       PRIMARY KEY (USR_ID), 
       FOREIGN KEY (USR_GROUP_ID)
                             REFERENCES T_GROUP
);

CREATE SEQUENCE SEQ_USER INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_FEATURE CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_FEATURE;





CREATE TABLE T_FEATURE (
       FT_ID                NUMBER NOT NULL,
       FT_ENTITY_ID         NUMBER NOT NULL,
       FT_ENTITY_TYPE       NUMBER NOT NULL,
       FT_KEY               VARCHAR2(128) NOT NULL,
       FT_NUMBER_VALUE      NUMBER NULL,
       FT_ANY_VALUE         BLOB NULL,
       FT_VALUE_STRING      VARCHAR2(4000) NULL,
       PRIMARY KEY (FT_ID)
);

CREATE SEQUENCE SEQ_FEATURE INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_LR_TYPE CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_LR_TYPE;





CREATE TABLE T_LR_TYPE (
       LRTP_ID              NUMBER NOT NULL,
       LRTP_TYPE            VARCHAR2(128) NOT NULL,
       PRIMARY KEY (LRTP_ID)
);

CREATE SEQUENCE SEQ_LR_TYPE INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_LANG_RESOURCE CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_LANG_RESOURCE;





CREATE TABLE T_LANG_RESOURCE (
       LR_ID                NUMBER NOT NULL,
       LR_TYPE_ID           NUMBER NOT NULL,
       LR_LOCKING_USER_ID   NUMBER NULL,
       LR_OWNER_ID          NUMBER NULL,
       LR_NAME              VARCHAR2(128) NOT NULL,
       LR_IS_PRIVATE        NUMBER(1) NOT NULL,
       LR_PARENT_ID         NUMBER NULL,
       PRIMARY KEY (LR_ID), 
       FOREIGN KEY (LR_LOCKING_USER_ID)
                             REFERENCES T_USER, 
       FOREIGN KEY (LR_OWNER_ID)
                             REFERENCES T_GROUP, 
       FOREIGN KEY (LR_PARENT_ID)
                             REFERENCES T_LANG_RESOURCE
);

CREATE SEQUENCE SEQ_LANG_RESOURCE INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_DOCUMENT CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_DOCUMENT;





CREATE TABLE T_DOCUMENT (
       DOC_ID               NUMBER NOT NULL,
       DOC_CONTENT_ID       NUMBER NULL,
       DOC_LR_ID            NUMBER NOT NULL,
       DOC_URL              VARCHAR2(4000) NOT NULL,
       DOC_START            NUMBER NULL,
       DOC_END              NUMBER NULL,
       DOC_IS_MARKUP_AWARE  NUMBER(1) NOT NULL,
       PRIMARY KEY (DOC_ID), 
       FOREIGN KEY (DOC_CONTENT_ID)
                             REFERENCES T_DOC_CONTENT, 
       FOREIGN KEY (DOC_LR_ID)
                             REFERENCES T_LANG_RESOURCE
);

CREATE SEQUENCE SEQ_DOCUMENT INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_NODE CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_NODE;





CREATE TABLE T_NODE (
       NODE_ID              NUMBER NOT NULL,
       NODE_DOC_ID          NUMBER NOT NULL,
       NODE_OFFSET          NUMBER NOT NULL,
       PRIMARY KEY (NODE_ID), 
       FOREIGN KEY (NODE_DOC_ID)
                             REFERENCES T_DOCUMENT
);

CREATE SEQUENCE SEQ_NODE INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_ANNOTATION_TYPE CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_ANNOTATION_TYPE;





CREATE TABLE T_ANNOTATION_TYPE (
       AT_ID                NUMBER NOT NULL,
       AT_NAME              VARCHAR2(16) NULL,
       PRIMARY KEY (AT_ID)
);

CREATE SEQUENCE SEQ_ANNOTATION_TYPE INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_ANNOTATION CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_ANNOTATION;





CREATE TABLE T_ANNOTATION (
       ANN_ID               NUMBER NOT NULL,
       ANN_AT_ID            NUMBER NOT NULL,
       ANN_ENDNODE_ID       NUMBER NOT NULL,
       ANN_STARTNODE_ID     NUMBER NOT NULL,
       PRIMARY KEY (ANN_ID), 
       FOREIGN KEY (ANN_STARTNODE_ID)
                             REFERENCES T_NODE, 
       FOREIGN KEY (ANN_ENDNODE_ID)
                             REFERENCES T_NODE, 
       FOREIGN KEY (ANN_AT_ID)
                             REFERENCES T_ANNOTATION_TYPE
);

CREATE SEQUENCE SEQ_ANNOTATION INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_ANNOT_SET CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_ANNOT_SET;





CREATE TABLE T_ANNOT_SET (
       AS_ID                NUMBER NOT NULL,
       AS_DOC_ID            NUMBER NOT NULL,
       AS_NAME              VARCHAR2(16) NULL,
       PRIMARY KEY (AS_ID), 
       FOREIGN KEY (AS_DOC_ID)
                             REFERENCES T_DOCUMENT
);

CREATE SEQUENCE SEQ_ANNOT_SET INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_AS_ANNOTATION CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_AS_ANNOTATION;





CREATE TABLE T_AS_ANNOTATION (
       ASANN_ID             NUMBER NOT NULL,
       ASANN_ANN_ID         NUMBER NOT NULL,
       ASANN_AS_ID          NUMBER NOT NULL,
       PRIMARY KEY (ASANN_ID), 
       FOREIGN KEY (ASANN_ANN_ID)
                             REFERENCES T_ANNOTATION, 
       FOREIGN KEY (ASANN_AS_ID)
                             REFERENCES T_ANNOT_SET
);

CREATE SEQUENCE SEQ_AS_ANNOTATION INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_CORPUS CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_CORPUS;





CREATE TABLE T_CORPUS (
       CORP_ID              NUMBER NOT NULL,
       CORP_LR_ID           NUMBER NOT NULL,
       PRIMARY KEY (CORP_ID), 
       FOREIGN KEY (CORP_LR_ID)
                             REFERENCES T_LANG_RESOURCE
);

CREATE SEQUENCE SEQ_CORPUS INCREMENT BY 1 START WITH 1;
    
   



DROP TABLE T_CORPUS_DOCUMENT CASCADE CONSTRAINTS;
  DROP SEQUENCE SEQ_CORPUS_DOCUMENT;





CREATE TABLE T_CORPUS_DOCUMENT (
       CD_ID                NUMBER NOT NULL,
       CD_CORP_ID           NUMBER NOT NULL,
       CD_DOC_ID            NUMBER NOT NULL,
       PRIMARY KEY (CD_ID), 
       FOREIGN KEY (CD_CORP_ID)
                             REFERENCES T_CORPUS, 
       FOREIGN KEY (CD_DOC_ID)
                             REFERENCES T_DOCUMENT
);

CREATE SEQUENCE SEQ_CORPUS_DOCUMENT INCREMENT BY 1 START WITH 1;
    
   





