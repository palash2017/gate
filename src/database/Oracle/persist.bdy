create or replace package body persist is

/*
 *  persist.bdy
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 18/Sep/2001
 *
 *  $Id$
 *
 */

  /*******************************************************************************************/
  procedure get_timestamp(p_timestamp  OUT number)
  is
  
  begin
       select SEQ_TIMESTAMP.nextval
       into p_timestamp
       from dual;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure get_lr_name(p_lr_id     IN number,
                        p_lr_name   OUT varchar2)
  is
  
  begin
       select lr_name
       into p_lr_name
       from t_lang_resource;

  exception
       when NO_DATA_FOUND then
          raise error.x_invalid_lr;

  end;                                                                                                        

  /*******************************************************************************************/
  procedure delete_lr(p_lr_id     IN number,
                      p_lr_type   IN varchar2)
  is
  
  begin
     raise error.x_not_implemented;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure create_lr(p_usr_id           IN number,
                      p_grp_id           IN number,
                      p_lr_type          IN varchar2,
                      p_lr_name          IN varchar2,
                      p_lr_permissions   IN number,
                      p_lr_parent_id     IN number,
                      p_lr_id            OUT number)
  is
    l_lr_type number;
  begin
     
     -- 1. sanity check
     if (false = security.is_valid_security_data(p_lr_permissions,p_grp_id,p_usr_id)) then
        raise error.x_incomplete_data;
     end if;
     
     -- 3. check if the LR type supplied is valid
     select lrtp_id
     into   l_lr_type
     from   t_lr_type
     where  lrtp_type = p_lr_type;
     
     
     -- 2. create a lang_resource record
     insert into t_lang_resource(lr_id,
                                 lr_type_id,
                                 lr_owner_user_id,
                                 lr_locking_user_id,
                                 lr_owner_group_id,
                                 lr_name,
                                 lr_access_mode,
                                 lr_parent_id)
     values (seq_lang_resource.nextval,
            l_lr_type,
            p_usr_id,
            null,
            p_grp_id,
            p_lr_name,
            p_lr_permissions,
            p_lr_parent_id)
     returning lr_id into p_lr_id;           
     
     
     exception
        when NO_DATA_FOUND then
           raise error.x_invalid_lr_type;
           
     
  end;                                                                                                        


  /*******************************************************************************************/
  procedure create_document(p_lr_id        IN number,
                            p_url          IN varchar2,
                            p_encoding     IN varchar2,
                            p_start_offset IN number,
                            p_end_offset   IN number,
                            p_is_mrk_aware IN number,
                            p_corpus_id    IN number,
                            p_doc_id       OUT number,
                            p_content_id   OUT number)
  is
  
  begin
     --1. create a document_content entry
     insert into t_doc_content(dc_id,
                               dc_encoding_id,
                               dc_content)
     values(seq_doc_content.nextval,
            p_encoding,
            empty_blob())
     returning dc_id into p_content_id;
     
     --2. create a document entry  
     insert into t_document(doc_id,
                            doc_content_id,
                            doc_lr_id,
                            doc_url,
                            doc_start,
                            doc_end,
                            doc_is_markup_aware)
     values(seq_document.nextval,
            p_content_id,
            p_lr_id,
            p_url,
            p_start_offset,
            p_end_offset,
            p_is_mrk_aware)
     returning doc_id into p_doc_id;
                 
     --3. if part of a corpus create a corpus_document entry
     if (p_corpus_id is not null) then
        insert into t_corpus_document(cd_id,
                                      cd_corp_id,
                                      cd_doc_id)
        values (seq_corpus_document.nextval,
                p_corpus_id,
                p_doc_id);                
     end if;     
                                     
  end;                                                                                                        
  
  
/*begin
  -- Initialization
  <Statement>; */
end persist;
/
