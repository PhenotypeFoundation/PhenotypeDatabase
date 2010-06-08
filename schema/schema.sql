--
-- PostgreSQL database dump
--

SET client_encoding = 'UNICODE';
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = true;

--
-- Name: _group; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE _group (
    id bigint NOT NULL,
    version bigint NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    last_updated timestamp without time zone,
    date_created timestamp without time zone,
    protect boolean NOT NULL
);


ALTER TABLE public._group OWNER TO gscf;

--
-- Name: _group_roles; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE _group_roles (
    role_id bigint NOT NULL,
    group_id bigint NOT NULL
);


ALTER TABLE public._group_roles OWNER TO gscf;

--
-- Name: _group_users; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE _group_users (
    user_base_id bigint NOT NULL,
    group_id bigint NOT NULL
);


ALTER TABLE public._group_users OWNER TO gscf;

--
-- Name: _role; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE _role (
    id bigint NOT NULL,
    version bigint NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    last_updated timestamp without time zone,
    date_created timestamp without time zone,
    protect boolean NOT NULL
);


ALTER TABLE public._role OWNER TO gscf;

--
-- Name: _role_users; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE _role_users (
    user_base_id bigint NOT NULL,
    role_id bigint NOT NULL
);


ALTER TABLE public._role_users OWNER TO gscf;

--
-- Name: _user; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE _user (
    id bigint NOT NULL,
    version bigint NOT NULL,
    "external" boolean NOT NULL,
    password_hash character varying(255),
    remoteapi boolean NOT NULL,
    username character varying(255) NOT NULL,
    federated boolean NOT NULL,
    action_hash character varying(255),
    profile_id bigint NOT NULL,
    enabled boolean NOT NULL,
    expiration timestamp without time zone,
    last_updated timestamp without time zone,
    federation_provider_id bigint,
    date_created timestamp without time zone,
    "class" character varying(255) NOT NULL
);


ALTER TABLE public._user OWNER TO gscf;

--
-- Name: _user__user; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE _user__user (
    user_base_follows_id bigint,
    user_base_id bigint,
    user_base_followers_id bigint
);


ALTER TABLE public._user__user OWNER TO gscf;

--
-- Name: _user_passwd_history; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE _user_passwd_history (
    user_base_id bigint,
    passwd_history_string character varying(255)
);


ALTER TABLE public._user_passwd_history OWNER TO gscf;

--
-- Name: address; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE address (
    id bigint NOT NULL,
    version bigint NOT NULL,
    post_code character varying(255),
    state character varying(255),
    city character varying(255),
    country character varying(255),
    category character varying(255) NOT NULL,
    suburb character varying(255),
    owner_id bigint NOT NULL,
    line1 character varying(255) NOT NULL,
    line3 character varying(255),
    line2 character varying(255)
);


ALTER TABLE public.address OWNER TO gscf;

--
-- Name: assay; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE assay (
    id bigint NOT NULL,
    version bigint NOT NULL,
    module_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    external_assayid bigint NOT NULL
);


ALTER TABLE public.assay OWNER TO gscf;

--
-- Name: assay_module; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE assay_module (
    id bigint NOT NULL,
    version bigint NOT NULL,
    platform character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    "type" character varying(255) NOT NULL,
    url character varying(255) NOT NULL
);


ALTER TABLE public.assay_module OWNER TO gscf;

--
-- Name: assay_sample; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE assay_sample (
    assay_samples_id bigint,
    sample_id bigint
);


ALTER TABLE public.assay_sample OWNER TO gscf;

--
-- Name: clinical_assay; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE clinical_assay (
    id bigint NOT NULL,
    version bigint NOT NULL,
    approved boolean NOT NULL,
    sop character varying(255),
    name character varying(255) NOT NULL,
    reference character varying(255),
    applied_method character varying(255)
);


ALTER TABLE public.clinical_assay OWNER TO gscf;

--
-- Name: clinical_assay_clinical_measurement; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE clinical_assay_clinical_measurement (
    clinical_assay_measurements_id bigint,
    clinical_measurement_id bigint
);


ALTER TABLE public.clinical_assay_clinical_measurement OWNER TO gscf;

--
-- Name: clinical_assay_instance; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE clinical_assay_instance (
    id bigint NOT NULL,
    version bigint NOT NULL,
    assay_id bigint NOT NULL
);


ALTER TABLE public.clinical_assay_instance OWNER TO gscf;

--
-- Name: clinical_float_data; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE clinical_float_data (
    id bigint NOT NULL,
    version bigint NOT NULL,
    assay_id bigint NOT NULL,
    value real NOT NULL,
    sample character varying(255) NOT NULL,
    measurement_id bigint NOT NULL
);


ALTER TABLE public.clinical_float_data OWNER TO gscf;

--
-- Name: clinical_string_data; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE clinical_string_data (
    id bigint NOT NULL,
    version bigint NOT NULL,
    assayid bigint NOT NULL,
    value real NOT NULL,
    sampleid bigint NOT NULL,
    measurement_id bigint NOT NULL
);


ALTER TABLE public.clinical_string_data OWNER TO gscf;

--
-- Name: compound; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE compound (
    id bigint NOT NULL,
    version bigint NOT NULL,
    is_carrier boolean NOT NULL,
    compound_id bigint,
    dose_unit character varying(255),
    name character varying(255) NOT NULL,
    dose real NOT NULL
);


ALTER TABLE public.compound OWNER TO gscf;

--
-- Name: details; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE details (
    id bigint NOT NULL,
    version bigint NOT NULL,
    logo character varying(255),
    description character varying(255),
    name character varying(255),
    logo_small character varying(255),
    display_name character varying(255),
    url_id bigint
);


ALTER TABLE public.details OWNER TO gscf;

--
-- Name: encrypted_data; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE encrypted_data (
    id character varying(32) NOT NULL,
    version bigint NOT NULL,
    data_item character varying(512) NOT NULL
);


ALTER TABLE public.encrypted_data OWNER TO gscf;

--
-- Name: event; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event (
    id bigint NOT NULL,
    version bigint NOT NULL,
    template_id bigint,
    end_time bigint NOT NULL,
    start_time bigint NOT NULL
);


ALTER TABLE public.event OWNER TO gscf;

--
-- Name: event_group; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_group (
    id bigint NOT NULL,
    version bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.event_group OWNER TO gscf;

--
-- Name: event_group_event; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_group_event (
    event_group_events_id bigint,
    event_id bigint
);


ALTER TABLE public.event_group_event OWNER TO gscf;

--
-- Name: event_group_subject; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_group_subject (
    event_group_subjects_id bigint,
    subject_id bigint
);


ALTER TABLE public.event_group_subject OWNER TO gscf;

--
-- Name: event_template_date_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_date_fields (
    event_id bigint,
    template_date_fields_date timestamp without time zone,
    template_date_fields_idx character varying(255),
    template_date_fields_elt timestamp without time zone NOT NULL
);


ALTER TABLE public.event_template_date_fields OWNER TO gscf;

--
-- Name: event_template_double_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_double_fields (
    event_id bigint,
    template_double_fields_double double precision,
    template_double_fields_idx character varying(255),
    template_double_fields_elt double precision NOT NULL
);


ALTER TABLE public.event_template_double_fields OWNER TO gscf;

--
-- Name: event_template_field; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_field (
    event_system_fields_id bigint,
    template_field_id bigint
);


ALTER TABLE public.event_template_field OWNER TO gscf;

--
-- Name: event_template_file_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_file_fields (
    event_id bigint,
    template_file_fields_string character varying(255),
    template_file_fields_idx character varying(255),
    template_file_fields_elt character varying(255) NOT NULL
);


ALTER TABLE public.event_template_file_fields OWNER TO gscf;

--
-- Name: event_template_float_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_float_fields (
    event_id bigint,
    template_float_fields_float real,
    template_float_fields_idx character varying(255),
    template_float_fields_elt real NOT NULL
);


ALTER TABLE public.event_template_float_fields OWNER TO gscf;

--
-- Name: event_template_integer_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_integer_fields (
    event_id bigint,
    template_integer_fields_int integer,
    template_integer_fields_idx character varying(255),
    template_integer_fields_elt integer NOT NULL
);


ALTER TABLE public.event_template_integer_fields OWNER TO gscf;

--
-- Name: event_template_rel_time_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_rel_time_fields (
    event_id bigint,
    template_rel_time_fields_long bigint,
    template_rel_time_fields_idx character varying(255),
    template_rel_time_fields_elt bigint NOT NULL
);


ALTER TABLE public.event_template_rel_time_fields OWNER TO gscf;

--
-- Name: event_template_string_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_string_fields (
    event_id bigint,
    template_string_fields_string character varying(255),
    template_string_fields_idx character varying(255),
    template_string_fields_elt character varying(255) NOT NULL
);


ALTER TABLE public.event_template_string_fields OWNER TO gscf;

--
-- Name: event_template_string_list_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_string_list_fields (
    event_template_string_list_fields_id bigint,
    template_field_list_item_id bigint,
    template_string_list_fields_idx character varying(255)
);


ALTER TABLE public.event_template_string_list_fields OWNER TO gscf;

--
-- Name: event_template_term_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_term_fields (
    event_template_term_fields_id bigint,
    term_id bigint,
    template_term_fields_idx character varying(255)
);


ALTER TABLE public.event_template_term_fields OWNER TO gscf;

--
-- Name: event_template_text_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE event_template_text_fields (
    event_id bigint,
    template_text_fields_string text,
    template_text_fields_idx character varying(255),
    template_text_fields_elt text NOT NULL
);


ALTER TABLE public.event_template_text_fields OWNER TO gscf;

--
-- Name: feature_base; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE feature_base (
    id bigint NOT NULL,
    version bigint NOT NULL,
    "type" character varying(255) NOT NULL,
    unit character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    "class" character varying(255) NOT NULL,
    is_intake boolean,
    is_drug boolean,
    correction_method character varying(255),
    detectable_limit real,
    in_serum boolean,
    reference_values character varying(255)
);


ALTER TABLE public.feature_base OWNER TO gscf;

--
-- Name: feature_base_term; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE feature_base_term (
    feature_base_compound_id bigint,
    term_id bigint,
    feature_base_metabolite_id bigint,
    feature_base_disease_id bigint,
    feature_base_enzyme_id bigint,
    feature_base_organism_part_id bigint,
    feature_base_drug_id bigint
);


ALTER TABLE public.feature_base_term OWNER TO gscf;

--
-- Name: federation_provider; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE federation_provider (
    id bigint NOT NULL,
    version bigint NOT NULL,
    uid character varying(255) NOT NULL,
    details_id bigint NOT NULL,
    auto_provision boolean NOT NULL
);


ALTER TABLE public.federation_provider OWNER TO gscf;

--
-- Name: federation_provider_props; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE federation_provider_props (
    props bigint,
    props_idx character varying(255),
    props_elt character varying(255) NOT NULL
);


ALTER TABLE public.federation_provider_props OWNER TO gscf;

--
-- Name: feed; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE feed (
    id bigint NOT NULL,
    version bigint NOT NULL,
    feed_url_id bigint NOT NULL,
    details_id bigint NOT NULL
);


ALTER TABLE public.feed OWNER TO gscf;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: gscf
--

CREATE SEQUENCE hibernate_sequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO gscf;

--
-- Name: import_mapping; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE import_mapping (
    id bigint NOT NULL,
    version bigint NOT NULL
);


ALTER TABLE public.import_mapping OWNER TO gscf;

--
-- Name: import_mapping_mapping_column; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE import_mapping_mapping_column (
    import_mapping_columns_id bigint,
    mapping_column_id bigint
);


ALTER TABLE public.import_mapping_mapping_column OWNER TO gscf;

--
-- Name: language; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE "language" (
    id bigint NOT NULL,
    version bigint NOT NULL,
    scheme character varying(255) NOT NULL
);


ALTER TABLE public."language" OWNER TO gscf;

--
-- Name: language_codes; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE language_codes (
    language_id bigint,
    codes_string character varying(255)
);


ALTER TABLE public.language_codes OWNER TO gscf;

--
-- Name: level_permission_fifth; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE level_permission_fifth (
    level_permission_id bigint,
    fifth_string character varying(255)
);


ALTER TABLE public.level_permission_fifth OWNER TO gscf;

--
-- Name: level_permission_first; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE level_permission_first (
    level_permission_id bigint,
    first_string character varying(255)
);


ALTER TABLE public.level_permission_first OWNER TO gscf;

--
-- Name: level_permission_fourth; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE level_permission_fourth (
    level_permission_id bigint,
    fourth_string character varying(255)
);


ALTER TABLE public.level_permission_fourth OWNER TO gscf;

--
-- Name: level_permission_second; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE level_permission_second (
    level_permission_id bigint,
    second_string character varying(255)
);


ALTER TABLE public.level_permission_second OWNER TO gscf;

--
-- Name: level_permission_sixth; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE level_permission_sixth (
    level_permission_id bigint,
    sixth_string character varying(255)
);


ALTER TABLE public.level_permission_sixth OWNER TO gscf;

--
-- Name: level_permission_third; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE level_permission_third (
    level_permission_id bigint,
    third_string character varying(255)
);


ALTER TABLE public.level_permission_third OWNER TO gscf;

--
-- Name: login_record; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE login_record (
    id bigint NOT NULL,
    version bigint NOT NULL,
    user_agent character varying(255) NOT NULL,
    owner_id bigint NOT NULL,
    last_updated timestamp without time zone,
    date_created timestamp without time zone,
    remote_host character varying(255) NOT NULL,
    remote_addr character varying(255) NOT NULL
);


ALTER TABLE public.login_record OWNER TO gscf;

--
-- Name: mapping_column; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE mapping_column (
    id bigint NOT NULL,
    version bigint NOT NULL,
    "index" integer NOT NULL,
    name character varying(255) NOT NULL,
    entity character varying(255) NOT NULL,
    value character varying(255) NOT NULL,
    templatefieldtype character varying(255) NOT NULL,
    property character varying(255) NOT NULL,
    identifier boolean NOT NULL
);


ALTER TABLE public.mapping_column OWNER TO gscf;

--
-- Name: ontology; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE ontology (
    id bigint NOT NULL,
    version bigint NOT NULL,
    description character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    ncbo_versioned_id integer NOT NULL,
    version_number character varying(255) NOT NULL,
    url character varying(255) NOT NULL,
    ncbo_id integer NOT NULL
);


ALTER TABLE public.ontology OWNER TO gscf;

--
-- Name: permission; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE permission (
    id bigint NOT NULL,
    version bigint NOT NULL,
    "type" character varying(255) NOT NULL,
    possible_actions character varying(255) NOT NULL,
    managed boolean NOT NULL,
    target character varying(255) NOT NULL,
    role_id bigint,
    group_id bigint,
    user_id bigint,
    actions character varying(255) NOT NULL,
    "class" character varying(255) NOT NULL
);


ALTER TABLE public.permission OWNER TO gscf;

--
-- Name: person; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE person (
    id bigint NOT NULL,
    version bigint NOT NULL,
    last_name character varying(255),
    phone character varying(255),
    fax character varying(255),
    title character varying(255),
    address character varying(255),
    email character varying(255),
    initials character varying(255),
    prefix character varying(255),
    gender character varying(255),
    first_name character varying(255),
    mobile character varying(255)
);


ALTER TABLE public.person OWNER TO gscf;

--
-- Name: person_affiliation; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE person_affiliation (
    id bigint NOT NULL,
    version bigint NOT NULL,
    institute character varying(255) NOT NULL,
    department character varying(255) NOT NULL
);


ALTER TABLE public.person_affiliation OWNER TO gscf;

--
-- Name: person_person_affiliation; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE person_person_affiliation (
    person_affiliations_id bigint,
    person_affiliation_id bigint
);


ALTER TABLE public.person_person_affiliation OWNER TO gscf;

--
-- Name: person_role; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE person_role (
    id bigint NOT NULL,
    version bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.person_role OWNER TO gscf;

--
-- Name: phone; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE phone (
    id bigint NOT NULL,
    version bigint NOT NULL,
    owner_id bigint NOT NULL,
    number character varying(255) NOT NULL,
    "type" character varying(255) NOT NULL
);


ALTER TABLE public.phone OWNER TO gscf;

--
-- Name: profile_base; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE profile_base (
    id bigint NOT NULL,
    version bigint NOT NULL,
    email_hash character varying(255),
    nick_name character varying(255),
    gravatar boolean NOT NULL,
    gender character varying(255),
    photo_type character varying(255),
    last_updated timestamp without time zone,
    current_status_id bigint,
    photo bytea,
    bio character varying(255),
    email character varying(255),
    dob timestamp without time zone,
    date_created timestamp without time zone,
    full_name character varying(255),
    non_verified_email character varying(255),
    "class" character varying(255) NOT NULL
);


ALTER TABLE public.profile_base OWNER TO gscf;

--
-- Name: profile_base_alternate_emails; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE profile_base_alternate_emails (
    profile_base_id bigint,
    alternate_emails_string character varying(255)
);


ALTER TABLE public.profile_base_alternate_emails OWNER TO gscf;

--
-- Name: profile_base_feed; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE profile_base_feed (
    profile_base_feeds_id bigint,
    feed_id bigint
);


ALTER TABLE public.profile_base_feed OWNER TO gscf;

--
-- Name: profile_base_url; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE profile_base_url (
    profile_base_websites_id bigint,
    url_id bigint
);


ALTER TABLE public.profile_base_url OWNER TO gscf;

--
-- Name: publication; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE publication (
    id bigint NOT NULL,
    version bigint NOT NULL,
    title character varying(255) NOT NULL,
    pub_medid character varying(255),
    authors_list character varying(255),
    doi character varying(255),
    comments character varying(255)
);


ALTER TABLE public.publication OWNER TO gscf;

--
-- Name: rel_time; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE rel_time (
    id bigint NOT NULL,
    version bigint NOT NULL,
    value bigint NOT NULL
);


ALTER TABLE public.rel_time OWNER TO gscf;

--
-- Name: sample; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample (
    id bigint NOT NULL,
    version bigint NOT NULL,
    template_id bigint,
    parent_subject_id bigint,
    material_id bigint NOT NULL,
    parent_event_id bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.sample OWNER TO gscf;

--
-- Name: sample_template_date_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_date_fields (
    sample_id bigint,
    template_date_fields_date timestamp without time zone,
    template_date_fields_idx character varying(255),
    template_date_fields_elt timestamp without time zone NOT NULL
);


ALTER TABLE public.sample_template_date_fields OWNER TO gscf;

--
-- Name: sample_template_double_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_double_fields (
    sample_id bigint,
    template_double_fields_double double precision,
    template_double_fields_idx character varying(255),
    template_double_fields_elt double precision NOT NULL
);


ALTER TABLE public.sample_template_double_fields OWNER TO gscf;

--
-- Name: sample_template_field; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_field (
    sample_system_fields_id bigint,
    template_field_id bigint
);


ALTER TABLE public.sample_template_field OWNER TO gscf;

--
-- Name: sample_template_file_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_file_fields (
    sample_id bigint,
    template_file_fields_string character varying(255),
    template_file_fields_idx character varying(255),
    template_file_fields_elt character varying(255) NOT NULL
);


ALTER TABLE public.sample_template_file_fields OWNER TO gscf;

--
-- Name: sample_template_float_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_float_fields (
    sample_id bigint,
    template_float_fields_float real,
    template_float_fields_idx character varying(255),
    template_float_fields_elt real NOT NULL
);


ALTER TABLE public.sample_template_float_fields OWNER TO gscf;

--
-- Name: sample_template_integer_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_integer_fields (
    sample_id bigint,
    template_integer_fields_int integer,
    template_integer_fields_idx character varying(255),
    template_integer_fields_elt integer NOT NULL
);


ALTER TABLE public.sample_template_integer_fields OWNER TO gscf;

--
-- Name: sample_template_rel_time_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_rel_time_fields (
    sample_id bigint,
    template_rel_time_fields_long bigint,
    template_rel_time_fields_idx character varying(255),
    template_rel_time_fields_elt bigint NOT NULL
);


ALTER TABLE public.sample_template_rel_time_fields OWNER TO gscf;

--
-- Name: sample_template_string_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_string_fields (
    sample_id bigint,
    template_string_fields_string character varying(255),
    template_string_fields_idx character varying(255),
    template_string_fields_elt character varying(255) NOT NULL
);


ALTER TABLE public.sample_template_string_fields OWNER TO gscf;

--
-- Name: sample_template_string_list_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_string_list_fields (
    sample_template_string_list_fields_id bigint,
    template_field_list_item_id bigint,
    template_string_list_fields_idx character varying(255)
);


ALTER TABLE public.sample_template_string_list_fields OWNER TO gscf;

--
-- Name: sample_template_term_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_term_fields (
    sample_template_term_fields_id bigint,
    term_id bigint,
    template_term_fields_idx character varying(255)
);


ALTER TABLE public.sample_template_term_fields OWNER TO gscf;

--
-- Name: sample_template_text_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sample_template_text_fields (
    sample_id bigint,
    template_text_fields_string text,
    template_text_fields_idx character varying(255),
    template_text_fields_elt text NOT NULL
);


ALTER TABLE public.sample_template_text_fields OWNER TO gscf;

--
-- Name: sampling_event; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE sampling_event (
    id bigint NOT NULL
);


ALTER TABLE public.sampling_event OWNER TO gscf;

--
-- Name: social_media_account; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE social_media_account (
    id bigint NOT NULL,
    version bigint NOT NULL,
    accountid character varying(255),
    username character varying(255),
    service_id bigint NOT NULL,
    owner_id bigint NOT NULL,
    profile_id bigint
);


ALTER TABLE public.social_media_account OWNER TO gscf;

--
-- Name: social_media_account_feed; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE social_media_account_feed (
    social_media_account_feeds_id bigint,
    feed_id bigint
);


ALTER TABLE public.social_media_account_feed OWNER TO gscf;

--
-- Name: social_media_account_url; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE social_media_account_url (
    social_media_account_urls_id bigint,
    url_id bigint
);


ALTER TABLE public.social_media_account_url OWNER TO gscf;

--
-- Name: social_media_service; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE social_media_service (
    id bigint NOT NULL,
    version bigint NOT NULL,
    uid character varying(255) NOT NULL,
    base_profile_url_id bigint,
    details_id bigint NOT NULL
);


ALTER TABLE public.social_media_service OWNER TO gscf;

--
-- Name: status; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE status (
    id bigint NOT NULL,
    version bigint NOT NULL,
    status character varying(255) NOT NULL,
    owner_id bigint NOT NULL,
    last_updated timestamp without time zone,
    date_created timestamp without time zone,
    url_id bigint
);


ALTER TABLE public.status OWNER TO gscf;

--
-- Name: study; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study (
    id bigint NOT NULL,
    version bigint NOT NULL,
    start_date timestamp without time zone NOT NULL,
    title character varying(255) NOT NULL,
    template_id bigint,
    last_updated timestamp without time zone NOT NULL,
    owner_id bigint,
    external_studyid bigint NOT NULL,
    date_created timestamp without time zone NOT NULL
);


ALTER TABLE public.study OWNER TO gscf;

--
-- Name: study__user; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study__user (
    study_editors_id bigint,
    user_id bigint,
    study_readers_id bigint
);


ALTER TABLE public.study__user OWNER TO gscf;

--
-- Name: study_assay; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_assay (
    study_assays_id bigint,
    assay_id bigint
);


ALTER TABLE public.study_assay OWNER TO gscf;

--
-- Name: study_event; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_event (
    study_events_id bigint,
    event_id bigint
);


ALTER TABLE public.study_event OWNER TO gscf;

--
-- Name: study_event_group; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_event_group (
    study_event_groups_id bigint,
    event_group_id bigint
);


ALTER TABLE public.study_event_group OWNER TO gscf;

--
-- Name: study_person; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_person (
    id bigint NOT NULL,
    version bigint NOT NULL,
    person_id bigint NOT NULL,
    role_id bigint NOT NULL
);


ALTER TABLE public.study_person OWNER TO gscf;

--
-- Name: study_publication; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_publication (
    study_publications_id bigint,
    publication_id bigint
);


ALTER TABLE public.study_publication OWNER TO gscf;

--
-- Name: study_sample; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_sample (
    study_samples_id bigint,
    sample_id bigint
);


ALTER TABLE public.study_sample OWNER TO gscf;

--
-- Name: study_sampling_event; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_sampling_event (
    study_sampling_events_id bigint,
    sampling_event_id bigint
);


ALTER TABLE public.study_sampling_event OWNER TO gscf;

--
-- Name: study_study_person; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_study_person (
    study_persons_id bigint,
    study_person_id bigint
);


ALTER TABLE public.study_study_person OWNER TO gscf;

--
-- Name: study_subject; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_subject (
    study_subjects_id bigint,
    subject_id bigint
);


ALTER TABLE public.study_subject OWNER TO gscf;

--
-- Name: study_template_date_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_date_fields (
    study_id bigint,
    template_date_fields_date timestamp without time zone,
    template_date_fields_idx character varying(255),
    template_date_fields_elt timestamp without time zone NOT NULL
);


ALTER TABLE public.study_template_date_fields OWNER TO gscf;

--
-- Name: study_template_double_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_double_fields (
    study_id bigint,
    template_double_fields_double double precision,
    template_double_fields_idx character varying(255),
    template_double_fields_elt double precision NOT NULL
);


ALTER TABLE public.study_template_double_fields OWNER TO gscf;

--
-- Name: study_template_field; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_field (
    study_system_fields_id bigint,
    template_field_id bigint
);


ALTER TABLE public.study_template_field OWNER TO gscf;

--
-- Name: study_template_file_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_file_fields (
    study_id bigint,
    template_file_fields_string character varying(255),
    template_file_fields_idx character varying(255),
    template_file_fields_elt character varying(255) NOT NULL
);


ALTER TABLE public.study_template_file_fields OWNER TO gscf;

--
-- Name: study_template_float_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_float_fields (
    study_id bigint,
    template_float_fields_float real,
    template_float_fields_idx character varying(255),
    template_float_fields_elt real NOT NULL
);


ALTER TABLE public.study_template_float_fields OWNER TO gscf;

--
-- Name: study_template_integer_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_integer_fields (
    study_id bigint,
    template_integer_fields_int integer,
    template_integer_fields_idx character varying(255),
    template_integer_fields_elt integer NOT NULL
);


ALTER TABLE public.study_template_integer_fields OWNER TO gscf;

--
-- Name: study_template_rel_time_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_rel_time_fields (
    study_id bigint,
    template_rel_time_fields_long bigint,
    template_rel_time_fields_idx character varying(255),
    template_rel_time_fields_elt bigint NOT NULL
);


ALTER TABLE public.study_template_rel_time_fields OWNER TO gscf;

--
-- Name: study_template_string_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_string_fields (
    study_id bigint,
    template_string_fields_string character varying(255),
    template_string_fields_idx character varying(255),
    template_string_fields_elt character varying(255) NOT NULL
);


ALTER TABLE public.study_template_string_fields OWNER TO gscf;

--
-- Name: study_template_string_list_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_string_list_fields (
    study_template_string_list_fields_id bigint,
    template_field_list_item_id bigint,
    template_string_list_fields_idx character varying(255)
);


ALTER TABLE public.study_template_string_list_fields OWNER TO gscf;

--
-- Name: study_template_term_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_term_fields (
    study_template_term_fields_id bigint,
    term_id bigint,
    template_term_fields_idx character varying(255)
);


ALTER TABLE public.study_template_term_fields OWNER TO gscf;

--
-- Name: study_template_text_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE study_template_text_fields (
    study_id bigint,
    template_text_fields_string character varying(255),
    template_text_fields_idx character varying(255),
    template_text_fields_elt character varying(255) NOT NULL
);


ALTER TABLE public.study_template_text_fields OWNER TO gscf;

--
-- Name: subject; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject (
    id bigint NOT NULL,
    version bigint NOT NULL,
    template_id bigint,
    species_id bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.subject OWNER TO gscf;

--
-- Name: subject_template_date_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_date_fields (
    subject_id bigint,
    template_date_fields_date timestamp without time zone,
    template_date_fields_idx character varying(255),
    template_date_fields_elt timestamp without time zone NOT NULL
);


ALTER TABLE public.subject_template_date_fields OWNER TO gscf;

--
-- Name: subject_template_double_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_double_fields (
    subject_id bigint,
    template_double_fields_double double precision,
    template_double_fields_idx character varying(255),
    template_double_fields_elt double precision NOT NULL
);


ALTER TABLE public.subject_template_double_fields OWNER TO gscf;

--
-- Name: subject_template_field; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_field (
    subject_system_fields_id bigint,
    template_field_id bigint
);


ALTER TABLE public.subject_template_field OWNER TO gscf;

--
-- Name: subject_template_file_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_file_fields (
    subject_id bigint,
    template_file_fields_string character varying(255),
    template_file_fields_idx character varying(255),
    template_file_fields_elt character varying(255) NOT NULL
);


ALTER TABLE public.subject_template_file_fields OWNER TO gscf;

--
-- Name: subject_template_float_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_float_fields (
    subject_id bigint,
    template_float_fields_float real,
    template_float_fields_idx character varying(255),
    template_float_fields_elt real NOT NULL
);


ALTER TABLE public.subject_template_float_fields OWNER TO gscf;

--
-- Name: subject_template_integer_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_integer_fields (
    subject_id bigint,
    template_integer_fields_int integer,
    template_integer_fields_idx character varying(255),
    template_integer_fields_elt integer NOT NULL
);


ALTER TABLE public.subject_template_integer_fields OWNER TO gscf;

--
-- Name: subject_template_rel_time_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_rel_time_fields (
    subject_id bigint,
    template_rel_time_fields_long bigint,
    template_rel_time_fields_idx character varying(255),
    template_rel_time_fields_elt bigint NOT NULL
);


ALTER TABLE public.subject_template_rel_time_fields OWNER TO gscf;

--
-- Name: subject_template_string_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_string_fields (
    subject_id bigint,
    template_string_fields_string character varying(255),
    template_string_fields_idx character varying(255),
    template_string_fields_elt character varying(255) NOT NULL
);


ALTER TABLE public.subject_template_string_fields OWNER TO gscf;

--
-- Name: subject_template_string_list_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_string_list_fields (
    subject_template_string_list_fields_id bigint,
    template_field_list_item_id bigint,
    template_string_list_fields_idx character varying(255)
);


ALTER TABLE public.subject_template_string_list_fields OWNER TO gscf;

--
-- Name: subject_template_term_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_term_fields (
    subject_template_term_fields_id bigint,
    term_id bigint,
    template_term_fields_idx character varying(255)
);


ALTER TABLE public.subject_template_term_fields OWNER TO gscf;

--
-- Name: subject_template_text_fields; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE subject_template_text_fields (
    subject_id bigint,
    template_text_fields_string text,
    template_text_fields_idx character varying(255),
    template_text_fields_elt text NOT NULL
);


ALTER TABLE public.subject_template_text_fields OWNER TO gscf;

--
-- Name: template; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE "template" (
    id bigint NOT NULL,
    version bigint NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    entity character varying(255) NOT NULL
);


ALTER TABLE public."template" OWNER TO gscf;

--
-- Name: template_field; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE template_field (
    id bigint NOT NULL,
    version bigint NOT NULL,
    unit character varying(255),
    preferred_identifier boolean NOT NULL,
    name character varying(255) NOT NULL,
    "type" character varying(255) NOT NULL,
    required boolean NOT NULL,
    "comment" text
);


ALTER TABLE public.template_field OWNER TO gscf;

--
-- Name: template_field_list_item; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE template_field_list_item (
    id bigint NOT NULL,
    version bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.template_field_list_item OWNER TO gscf;

--
-- Name: template_field_ontology; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE template_field_ontology (
    template_field_ontologies_id bigint,
    ontology_id bigint
);


ALTER TABLE public.template_field_ontology OWNER TO gscf;

--
-- Name: template_field_template_field_list_item; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE template_field_template_field_list_item (
    template_field_list_entries_id bigint,
    template_field_list_item_id bigint,
    list_entries_idx integer
);


ALTER TABLE public.template_field_template_field_list_item OWNER TO gscf;

--
-- Name: template_template_field; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE template_template_field (
    template_fields_id bigint,
    template_field_id bigint,
    fields_idx integer
);


ALTER TABLE public.template_template_field OWNER TO gscf;

--
-- Name: term; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE term (
    id bigint NOT NULL,
    version bigint NOT NULL,
    name character varying(255) NOT NULL,
    accession character varying(255) NOT NULL,
    ontology_id bigint NOT NULL
);


ALTER TABLE public.term OWNER TO gscf;

--
-- Name: url; Type: TABLE; Schema: public; Owner: gscf; Tablespace: 
--

CREATE TABLE url (
    id bigint NOT NULL,
    version bigint NOT NULL,
    "location" character varying(255) NOT NULL,
    description character varying(255),
    name character varying(255),
    alt_text character varying(255),
    lang_id bigint
);


ALTER TABLE public.url OWNER TO gscf;

--
-- Name: _group_name_key; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY _group
    ADD CONSTRAINT _group_name_key UNIQUE (name);


ALTER INDEX public._group_name_key OWNER TO gscf;

--
-- Name: _group_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY _group
    ADD CONSTRAINT _group_pkey PRIMARY KEY (id);


ALTER INDEX public._group_pkey OWNER TO gscf;

--
-- Name: _group_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY _group_roles
    ADD CONSTRAINT _group_roles_pkey PRIMARY KEY (group_id, role_id);


ALTER INDEX public._group_roles_pkey OWNER TO gscf;

--
-- Name: _group_users_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY _group_users
    ADD CONSTRAINT _group_users_pkey PRIMARY KEY (group_id, user_base_id);


ALTER INDEX public._group_users_pkey OWNER TO gscf;

--
-- Name: _role_name_key; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY _role
    ADD CONSTRAINT _role_name_key UNIQUE (name);


ALTER INDEX public._role_name_key OWNER TO gscf;

--
-- Name: _role_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY _role
    ADD CONSTRAINT _role_pkey PRIMARY KEY (id);


ALTER INDEX public._role_pkey OWNER TO gscf;

--
-- Name: _role_users_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY _role_users
    ADD CONSTRAINT _role_users_pkey PRIMARY KEY (role_id, user_base_id);


ALTER INDEX public._role_users_pkey OWNER TO gscf;

--
-- Name: _user_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY _user
    ADD CONSTRAINT _user_pkey PRIMARY KEY (id);


ALTER INDEX public._user_pkey OWNER TO gscf;

--
-- Name: _user_username_key; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY _user
    ADD CONSTRAINT _user_username_key UNIQUE (username);


ALTER INDEX public._user_username_key OWNER TO gscf;

--
-- Name: address_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY address
    ADD CONSTRAINT address_pkey PRIMARY KEY (id);


ALTER INDEX public.address_pkey OWNER TO gscf;

--
-- Name: assay_module_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY assay_module
    ADD CONSTRAINT assay_module_pkey PRIMARY KEY (id);


ALTER INDEX public.assay_module_pkey OWNER TO gscf;

--
-- Name: assay_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY assay
    ADD CONSTRAINT assay_pkey PRIMARY KEY (id);


ALTER INDEX public.assay_pkey OWNER TO gscf;

--
-- Name: clinical_assay_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY clinical_assay_instance
    ADD CONSTRAINT clinical_assay_instance_pkey PRIMARY KEY (id);


ALTER INDEX public.clinical_assay_instance_pkey OWNER TO gscf;

--
-- Name: clinical_assay_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY clinical_assay
    ADD CONSTRAINT clinical_assay_pkey PRIMARY KEY (id);


ALTER INDEX public.clinical_assay_pkey OWNER TO gscf;

--
-- Name: clinical_float_data_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY clinical_float_data
    ADD CONSTRAINT clinical_float_data_pkey PRIMARY KEY (id);


ALTER INDEX public.clinical_float_data_pkey OWNER TO gscf;

--
-- Name: clinical_string_data_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY clinical_string_data
    ADD CONSTRAINT clinical_string_data_pkey PRIMARY KEY (id);


ALTER INDEX public.clinical_string_data_pkey OWNER TO gscf;

--
-- Name: compound_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY compound
    ADD CONSTRAINT compound_pkey PRIMARY KEY (id);


ALTER INDEX public.compound_pkey OWNER TO gscf;

--
-- Name: details_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY details
    ADD CONSTRAINT details_pkey PRIMARY KEY (id);


ALTER INDEX public.details_pkey OWNER TO gscf;

--
-- Name: encrypted_data_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY encrypted_data
    ADD CONSTRAINT encrypted_data_pkey PRIMARY KEY (id);


ALTER INDEX public.encrypted_data_pkey OWNER TO gscf;

--
-- Name: event_group_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY event_group
    ADD CONSTRAINT event_group_pkey PRIMARY KEY (id);


ALTER INDEX public.event_group_pkey OWNER TO gscf;

--
-- Name: event_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_pkey PRIMARY KEY (id);


ALTER INDEX public.event_pkey OWNER TO gscf;

--
-- Name: feature_base_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY feature_base
    ADD CONSTRAINT feature_base_pkey PRIMARY KEY (id);


ALTER INDEX public.feature_base_pkey OWNER TO gscf;

--
-- Name: federation_provider_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY federation_provider
    ADD CONSTRAINT federation_provider_pkey PRIMARY KEY (id);


ALTER INDEX public.federation_provider_pkey OWNER TO gscf;

--
-- Name: feed_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY feed
    ADD CONSTRAINT feed_pkey PRIMARY KEY (id);


ALTER INDEX public.feed_pkey OWNER TO gscf;

--
-- Name: import_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY import_mapping
    ADD CONSTRAINT import_mapping_pkey PRIMARY KEY (id);


ALTER INDEX public.import_mapping_pkey OWNER TO gscf;

--
-- Name: language_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY "language"
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


ALTER INDEX public.language_pkey OWNER TO gscf;

--
-- Name: login_record_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY login_record
    ADD CONSTRAINT login_record_pkey PRIMARY KEY (id);


ALTER INDEX public.login_record_pkey OWNER TO gscf;

--
-- Name: mapping_column_name_key; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY mapping_column
    ADD CONSTRAINT mapping_column_name_key UNIQUE (name);


ALTER INDEX public.mapping_column_name_key OWNER TO gscf;

--
-- Name: mapping_column_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY mapping_column
    ADD CONSTRAINT mapping_column_pkey PRIMARY KEY (id);


ALTER INDEX public.mapping_column_pkey OWNER TO gscf;

--
-- Name: ontology_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY ontology
    ADD CONSTRAINT ontology_pkey PRIMARY KEY (id);


ALTER INDEX public.ontology_pkey OWNER TO gscf;

--
-- Name: permission_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (id);


ALTER INDEX public.permission_pkey OWNER TO gscf;

--
-- Name: person_affiliation_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY person_affiliation
    ADD CONSTRAINT person_affiliation_pkey PRIMARY KEY (id);


ALTER INDEX public.person_affiliation_pkey OWNER TO gscf;

--
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);


ALTER INDEX public.person_pkey OWNER TO gscf;

--
-- Name: person_role_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY person_role
    ADD CONSTRAINT person_role_pkey PRIMARY KEY (id);


ALTER INDEX public.person_role_pkey OWNER TO gscf;

--
-- Name: phone_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY phone
    ADD CONSTRAINT phone_pkey PRIMARY KEY (id);


ALTER INDEX public.phone_pkey OWNER TO gscf;

--
-- Name: profile_base_email_key; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY profile_base
    ADD CONSTRAINT profile_base_email_key UNIQUE (email);


ALTER INDEX public.profile_base_email_key OWNER TO gscf;

--
-- Name: profile_base_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY profile_base
    ADD CONSTRAINT profile_base_pkey PRIMARY KEY (id);


ALTER INDEX public.profile_base_pkey OWNER TO gscf;

--
-- Name: publication_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_pkey PRIMARY KEY (id);


ALTER INDEX public.publication_pkey OWNER TO gscf;

--
-- Name: rel_time_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY rel_time
    ADD CONSTRAINT rel_time_pkey PRIMARY KEY (id);


ALTER INDEX public.rel_time_pkey OWNER TO gscf;

--
-- Name: sample_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY sample
    ADD CONSTRAINT sample_pkey PRIMARY KEY (id);


ALTER INDEX public.sample_pkey OWNER TO gscf;

--
-- Name: sampling_event_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY sampling_event
    ADD CONSTRAINT sampling_event_pkey PRIMARY KEY (id);


ALTER INDEX public.sampling_event_pkey OWNER TO gscf;

--
-- Name: social_media_account_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY social_media_account
    ADD CONSTRAINT social_media_account_pkey PRIMARY KEY (id);


ALTER INDEX public.social_media_account_pkey OWNER TO gscf;

--
-- Name: social_media_service_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY social_media_service
    ADD CONSTRAINT social_media_service_pkey PRIMARY KEY (id);


ALTER INDEX public.social_media_service_pkey OWNER TO gscf;

--
-- Name: status_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY status
    ADD CONSTRAINT status_pkey PRIMARY KEY (id);


ALTER INDEX public.status_pkey OWNER TO gscf;

--
-- Name: study_person_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY study_person
    ADD CONSTRAINT study_person_pkey PRIMARY KEY (id);


ALTER INDEX public.study_person_pkey OWNER TO gscf;

--
-- Name: study_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY study
    ADD CONSTRAINT study_pkey PRIMARY KEY (id);


ALTER INDEX public.study_pkey OWNER TO gscf;

--
-- Name: subject_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT subject_pkey PRIMARY KEY (id);


ALTER INDEX public.subject_pkey OWNER TO gscf;

--
-- Name: template_field_list_item_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY template_field_list_item
    ADD CONSTRAINT template_field_list_item_pkey PRIMARY KEY (id);


ALTER INDEX public.template_field_list_item_pkey OWNER TO gscf;

--
-- Name: template_field_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY template_field
    ADD CONSTRAINT template_field_pkey PRIMARY KEY (id);


ALTER INDEX public.template_field_pkey OWNER TO gscf;

--
-- Name: template_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY "template"
    ADD CONSTRAINT template_pkey PRIMARY KEY (id);


ALTER INDEX public.template_pkey OWNER TO gscf;

--
-- Name: term_ontology_id_key; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY term
    ADD CONSTRAINT term_ontology_id_key UNIQUE (ontology_id, accession);


ALTER INDEX public.term_ontology_id_key OWNER TO gscf;

--
-- Name: term_ontology_id_key1; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY term
    ADD CONSTRAINT term_ontology_id_key1 UNIQUE (ontology_id, name);


ALTER INDEX public.term_ontology_id_key1 OWNER TO gscf;

--
-- Name: term_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY term
    ADD CONSTRAINT term_pkey PRIMARY KEY (id);


ALTER INDEX public.term_pkey OWNER TO gscf;

--
-- Name: url_pkey; Type: CONSTRAINT; Schema: public; Owner: gscf; Tablespace: 
--

ALTER TABLE ONLY url
    ADD CONSTRAINT url_pkey PRIMARY KEY (id);


ALTER INDEX public.url_pkey OWNER TO gscf;

--
-- Name: fk14e7a8b6de1700f4; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_publication
    ADD CONSTRAINT fk14e7a8b6de1700f4 FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: fk14e7a8b6f25e0620; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_publication
    ADD CONSTRAINT fk14e7a8b6f25e0620 FOREIGN KEY (study_publications_id) REFERENCES study(id);


--
-- Name: fk16000e0bd3a394a; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_sample
    ADD CONSTRAINT fk16000e0bd3a394a FOREIGN KEY (study_samples_id) REFERENCES study(id);


--
-- Name: fk16000e0cbf5fe80; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_sample
    ADD CONSTRAINT fk16000e0cbf5fe80 FOREIGN KEY (sample_id) REFERENCES sample(id);


--
-- Name: fk1c56f208902c6; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY url
    ADD CONSTRAINT fk1c56f208902c6 FOREIGN KEY (lang_id) REFERENCES "language"(id);


--
-- Name: fk1c8b2b3e123bda38; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _group_roles
    ADD CONSTRAINT fk1c8b2b3e123bda38 FOREIGN KEY (group_id) REFERENCES _group(id);


--
-- Name: fk1c8b2b3eac8a955c; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _group_roles
    ADD CONSTRAINT fk1c8b2b3eac8a955c FOREIGN KEY (role_id) REFERENCES _role(id);


--
-- Name: fk1cb72a89123bda38; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _group_users
    ADD CONSTRAINT fk1cb72a89123bda38 FOREIGN KEY (group_id) REFERENCES _group(id);


--
-- Name: fk1cb72a89bb02cc73; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _group_users
    ADD CONSTRAINT fk1cb72a89bb02cc73 FOREIGN KEY (user_base_id) REFERENCES _user(id);


--
-- Name: fk25943b6d33e6dfd8; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY federation_provider
    ADD CONSTRAINT fk25943b6d33e6dfd8 FOREIGN KEY (details_id) REFERENCES details(id);


--
-- Name: fk2aa86afd2046a45c; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY social_media_account_feed
    ADD CONSTRAINT fk2aa86afd2046a45c FOREIGN KEY (feed_id) REFERENCES feed(id);


--
-- Name: fk2aa86afdb9eec27c; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY social_media_account_feed
    ADD CONSTRAINT fk2aa86afdb9eec27c FOREIGN KEY (social_media_account_feeds_id) REFERENCES social_media_account(id);


--
-- Name: fk2fe59e33e6dfd8; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY feed
    ADD CONSTRAINT fk2fe59e33e6dfd8 FOREIGN KEY (details_id) REFERENCES details(id);


--
-- Name: fk2fe59e8c1dfd79; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY feed
    ADD CONSTRAINT fk2fe59e8c1dfd79 FOREIGN KEY (feed_url_id) REFERENCES url(id);


--
-- Name: fk36446ca83f7e90; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY term
    ADD CONSTRAINT fk36446ca83f7e90 FOREIGN KEY (ontology_id) REFERENCES ontology(id);


--
-- Name: fk37d7f15922f11b7d; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY template_field_ontology
    ADD CONSTRAINT fk37d7f15922f11b7d FOREIGN KEY (template_field_ontologies_id) REFERENCES template_field(id);


--
-- Name: fk37d7f159a83f7e90; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY template_field_ontology
    ADD CONSTRAINT fk37d7f159a83f7e90 FOREIGN KEY (ontology_id) REFERENCES ontology(id);


--
-- Name: fk3ad8eb5a1953ed7f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY template_template_field
    ADD CONSTRAINT fk3ad8eb5a1953ed7f FOREIGN KEY (template_field_id) REFERENCES template_field(id);


--
-- Name: fk3c9d7e30cbf5fe80; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY assay_sample
    ADD CONSTRAINT fk3c9d7e30cbf5fe80 FOREIGN KEY (sample_id) REFERENCES sample(id);


--
-- Name: fk3c9d7e30f6da6b4a; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY assay_sample
    ADD CONSTRAINT fk3c9d7e30f6da6b4a FOREIGN KEY (assay_samples_id) REFERENCES assay(id);


--
-- Name: fk3ce43f4264f45c34; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY clinical_assay_clinical_measurement
    ADD CONSTRAINT fk3ce43f4264f45c34 FOREIGN KEY (clinical_assay_measurements_id) REFERENCES clinical_assay(id);


--
-- Name: fk3ce43f42a1cebcf0; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY clinical_assay_clinical_measurement
    ADD CONSTRAINT fk3ce43f42a1cebcf0 FOREIGN KEY (clinical_measurement_id) REFERENCES feature_base(id);


--
-- Name: fk40e2022b1953ed7f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_template_field
    ADD CONSTRAINT fk40e2022b1953ed7f FOREIGN KEY (template_field_id) REFERENCES template_field(id);


--
-- Name: fk40e2022b469f7a0a; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_template_field
    ADD CONSTRAINT fk40e2022b469f7a0a FOREIGN KEY (study_system_fields_id) REFERENCES study(id);


--
-- Name: fk4198d1782ad5a965; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_sampling_event
    ADD CONSTRAINT fk4198d1782ad5a965 FOREIGN KEY (sampling_event_id) REFERENCES sampling_event(id);


--
-- Name: fk4198d1789ce8fde2; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_sampling_event
    ADD CONSTRAINT fk4198d1789ce8fde2 FOREIGN KEY (study_sampling_events_id) REFERENCES study(id);


--
-- Name: fk48ead7a78f873816; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY profile_base
    ADD CONSTRAINT fk48ead7a78f873816 FOREIGN KEY (current_status_id) REFERENCES status(id);


--
-- Name: fk4c2330b690958e20; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk4c2330b690958e20 FOREIGN KEY (study_subjects_id) REFERENCES study(id);


--
-- Name: fk4c2330b6a98e8df4; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk4c2330b6a98e8df4 FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- Name: fk4cd0f1416021e487; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_study_person
    ADD CONSTRAINT fk4cd0f1416021e487 FOREIGN KEY (study_person_id) REFERENCES study_person(id);


--
-- Name: fk4cd0f141dd259175; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_study_person
    ADD CONSTRAINT fk4cd0f141dd259175 FOREIGN KEY (study_persons_id) REFERENCES study(id);


--
-- Name: fk4d4a8681953ed7f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY subject_template_field
    ADD CONSTRAINT fk4d4a8681953ed7f FOREIGN KEY (template_field_id) REFERENCES template_field(id);


--
-- Name: fk4d4a86844f125ea; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY subject_template_field
    ADD CONSTRAINT fk4d4a86844f125ea FOREIGN KEY (subject_system_fields_id) REFERENCES subject(id);


--
-- Name: fk4f5e54592dcc7e59; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY clinical_float_data
    ADD CONSTRAINT fk4f5e54592dcc7e59 FOREIGN KEY (assay_id) REFERENCES clinical_assay_instance(id);


--
-- Name: fk4f5e5459c4f3e44; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY clinical_float_data
    ADD CONSTRAINT fk4f5e5459c4f3e44 FOREIGN KEY (measurement_id) REFERENCES feature_base(id);


--
-- Name: fk540247fdd8d911c; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY language_codes
    ADD CONSTRAINT fk540247fdd8d911c FOREIGN KEY (language_id) REFERENCES "language"(id);


--
-- Name: fk571a4aa66a8f1e9; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _user
    ADD CONSTRAINT fk571a4aa66a8f1e9 FOREIGN KEY (profile_id) REFERENCES profile_base(id);


--
-- Name: fk571a4aaa84cbd63; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _user
    ADD CONSTRAINT fk571a4aaa84cbd63 FOREIGN KEY (federation_provider_id) REFERENCES federation_provider(id);


--
-- Name: fk58cea793ef1f0cd; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY assay
    ADD CONSTRAINT fk58cea793ef1f0cd FOREIGN KEY (module_id) REFERENCES assay_module(id);


--
-- Name: fk5c6729a4247a300; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY event
    ADD CONSTRAINT fk5c6729a4247a300 FOREIGN KEY (template_id) REFERENCES "template"(id);


--
-- Name: fk5cd8f242239fd3b8; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY details
    ADD CONSTRAINT fk5cd8f242239fd3b8 FOREIGN KEY (url_id) REFERENCES url(id);


--
-- Name: fk5eb5768fcab6b3e1; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY level_permission_fourth
    ADD CONSTRAINT fk5eb5768fcab6b3e1 FOREIGN KEY (level_permission_id) REFERENCES permission(id);


--
-- Name: fk607d35542140fb2b; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY profile_base_alternate_emails
    ADD CONSTRAINT fk607d35542140fb2b FOREIGN KEY (profile_base_id) REFERENCES profile_base(id);


--
-- Name: fk65b3d6eb83749f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY phone
    ADD CONSTRAINT fk65b3d6eb83749f FOREIGN KEY (owner_id) REFERENCES profile_base(id);


--
-- Name: fk68b0dc94247a300; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study
    ADD CONSTRAINT fk68b0dc94247a300 FOREIGN KEY (template_id) REFERENCES "template"(id);


--
-- Name: fk68b0dc9750752d7; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study
    ADD CONSTRAINT fk68b0dc9750752d7 FOREIGN KEY (owner_id) REFERENCES _user(id);


--
-- Name: fk6c251e301549da89; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY person_person_affiliation
    ADD CONSTRAINT fk6c251e301549da89 FOREIGN KEY (person_affiliation_id) REFERENCES person_affiliation(id);


--
-- Name: fk6c251e306dd0eac8; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY person_person_affiliation
    ADD CONSTRAINT fk6c251e306dd0eac8 FOREIGN KEY (person_affiliations_id) REFERENCES person(id);


--
-- Name: fk6f51f72458ff6d8e; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_event_group
    ADD CONSTRAINT fk6f51f72458ff6d8e FOREIGN KEY (study_event_groups_id) REFERENCES study(id);


--
-- Name: fk6f51f724aa6618eb; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_event_group
    ADD CONSTRAINT fk6f51f724aa6618eb FOREIGN KEY (event_group_id) REFERENCES event_group(id);


--
-- Name: fk744f50e9cab6b3e1; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY level_permission_second
    ADD CONSTRAINT fk744f50e9cab6b3e1 FOREIGN KEY (level_permission_id) REFERENCES permission(id);


--
-- Name: fk7c546ab56f1f15b4; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY event_group_event
    ADD CONSTRAINT fk7c546ab56f1f15b4 FOREIGN KEY (event_id) REFERENCES event(id);


--
-- Name: fk7c546ab5d78656a7; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY event_group_event
    ADD CONSTRAINT fk7c546ab5d78656a7 FOREIGN KEY (event_group_events_id) REFERENCES event_group(id);


--
-- Name: fk8dc39830239fd3b8; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY social_media_account_url
    ADD CONSTRAINT fk8dc39830239fd3b8 FOREIGN KEY (url_id) REFERENCES url(id);


--
-- Name: fk8dc39830947a1e2f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY social_media_account_url
    ADD CONSTRAINT fk8dc39830947a1e2f FOREIGN KEY (social_media_account_urls_id) REFERENCES social_media_account(id);


--
-- Name: fk90aa552c4247a300; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT fk90aa552c4247a300 FOREIGN KEY (template_id) REFERENCES "template"(id);


--
-- Name: fk90aa552c8cb6e9a0; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT fk90aa552c8cb6e9a0 FOREIGN KEY (species_id) REFERENCES term(id);


--
-- Name: fk939fdab457ed4b8d; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study__user
    ADD CONSTRAINT fk939fdab457ed4b8d FOREIGN KEY (study_editors_id) REFERENCES study(id);


--
-- Name: fk939fdab4920a2bf; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study__user
    ADD CONSTRAINT fk939fdab4920a2bf FOREIGN KEY (user_id) REFERENCES _user(id);


--
-- Name: fk939fdab4b56ee7e3; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study__user
    ADD CONSTRAINT fk939fdab4b56ee7e3 FOREIGN KEY (study_readers_id) REFERENCES study(id);


--
-- Name: fk93bb208347dd1554; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_assay
    ADD CONSTRAINT fk93bb208347dd1554 FOREIGN KEY (assay_id) REFERENCES assay(id);


--
-- Name: fk93bb208367e6cf6d; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_assay
    ADD CONSTRAINT fk93bb208367e6cf6d FOREIGN KEY (study_assays_id) REFERENCES study(id);


--
-- Name: fk93f4a8a421ed5f0e; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk93f4a8a421ed5f0e FOREIGN KEY (study_events_id) REFERENCES study(id);


--
-- Name: fk93f4a8a46f1f15b4; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk93f4a8a46f1f15b4 FOREIGN KEY (event_id) REFERENCES event(id);


--
-- Name: fk9a278becc4f3e44; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY clinical_string_data
    ADD CONSTRAINT fk9a278becc4f3e44 FOREIGN KEY (measurement_id) REFERENCES feature_base(id);


--
-- Name: fk9b9ab287a98e8df4; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY event_group_subject
    ADD CONSTRAINT fk9b9ab287a98e8df4 FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- Name: fk9b9ab287d7e06579; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY event_group_subject
    ADD CONSTRAINT fk9b9ab287d7e06579 FOREIGN KEY (event_group_subjects_id) REFERENCES event_group(id);


--
-- Name: fka305a0d7239fd3b8; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY profile_base_url
    ADD CONSTRAINT fka305a0d7239fd3b8 FOREIGN KEY (url_id) REFERENCES url(id);


--
-- Name: fka305a0d7e0ef9d62; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY profile_base_url
    ADD CONSTRAINT fka305a0d7e0ef9d62 FOREIGN KEY (profile_base_websites_id) REFERENCES profile_base(id);


--
-- Name: fka4c16028bb02cc73; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _user_passwd_history
    ADD CONSTRAINT fka4c16028bb02cc73 FOREIGN KEY (user_base_id) REFERENCES _user(id);


--
-- Name: fka708413a1953ed7f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY event_template_field
    ADD CONSTRAINT fka708413a1953ed7f FOREIGN KEY (template_field_id) REFERENCES template_field(id);


--
-- Name: fka708413abb6d962a; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY event_template_field
    ADD CONSTRAINT fka708413abb6d962a FOREIGN KEY (event_system_fields_id) REFERENCES event(id);


--
-- Name: fka9e4a00833e6dfd8; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY social_media_service
    ADD CONSTRAINT fka9e4a00833e6dfd8 FOREIGN KEY (details_id) REFERENCES details(id);


--
-- Name: fka9e4a008fa33923c; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY social_media_service
    ADD CONSTRAINT fka9e4a008fa33923c FOREIGN KEY (base_profile_url_id) REFERENCES url(id);


--
-- Name: fkadd6169eac8a955c; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _role_users
    ADD CONSTRAINT fkadd6169eac8a955c FOREIGN KEY (role_id) REFERENCES _role(id);


--
-- Name: fkadd6169ebb02cc73; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _role_users
    ADD CONSTRAINT fkadd6169ebb02cc73 FOREIGN KEY (user_base_id) REFERENCES _user(id);


--
-- Name: fkb31804d568dd785d; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _user__user
    ADD CONSTRAINT fkb31804d568dd785d FOREIGN KEY (user_base_followers_id) REFERENCES _user(id);


--
-- Name: fkb31804d5bb02cc73; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _user__user
    ADD CONSTRAINT fkb31804d5bb02cc73 FOREIGN KEY (user_base_id) REFERENCES _user(id);


--
-- Name: fkb31804d5d3d018f0; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY _user__user
    ADD CONSTRAINT fkb31804d5d3d018f0 FOREIGN KEY (user_base_follows_id) REFERENCES _user(id);


--
-- Name: fkbb979bf4b83749f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY address
    ADD CONSTRAINT fkbb979bf4b83749f FOREIGN KEY (owner_id) REFERENCES profile_base(id);


--
-- Name: fkbda777362046a45c; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY profile_base_feed
    ADD CONSTRAINT fkbda777362046a45c FOREIGN KEY (feed_id) REFERENCES feed(id);


--
-- Name: fkbda77736caa47b15; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY profile_base_feed
    ADD CONSTRAINT fkbda77736caa47b15 FOREIGN KEY (profile_base_feeds_id) REFERENCES profile_base(id);


--
-- Name: fkc3b4e354369e19fb; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY template_field_template_field_list_item
    ADD CONSTRAINT fkc3b4e354369e19fb FOREIGN KEY (template_field_list_item_id) REFERENCES template_field_list_item(id);


--
-- Name: fkc3edd55239f59329; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY import_mapping_mapping_column
    ADD CONSTRAINT fkc3edd55239f59329 FOREIGN KEY (mapping_column_id) REFERENCES mapping_column(id);


--
-- Name: fkc3edd552aec07603; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY import_mapping_mapping_column
    ADD CONSTRAINT fkc3edd552aec07603 FOREIGN KEY (import_mapping_columns_id) REFERENCES import_mapping(id);


--
-- Name: fkc93cb6a2cab6b3e1; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY level_permission_fifth
    ADD CONSTRAINT fkc93cb6a2cab6b3e1 FOREIGN KEY (level_permission_id) REFERENCES permission(id);


--
-- Name: fkc93ce39bcab6b3e1; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY level_permission_first
    ADD CONSTRAINT fkc93ce39bcab6b3e1 FOREIGN KEY (level_permission_id) REFERENCES permission(id);


--
-- Name: fkc9c775aa27db9c02; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY sample
    ADD CONSTRAINT fkc9c775aa27db9c02 FOREIGN KEY (parent_event_id) REFERENCES sampling_event(id);


--
-- Name: fkc9c775aa2f40b9d5; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY sample
    ADD CONSTRAINT fkc9c775aa2f40b9d5 FOREIGN KEY (material_id) REFERENCES term(id);


--
-- Name: fkc9c775aa4247a300; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY sample
    ADD CONSTRAINT fkc9c775aa4247a300 FOREIGN KEY (template_id) REFERENCES "template"(id);


--
-- Name: fkc9c775aac604aca9; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY sample
    ADD CONSTRAINT fkc9c775aac604aca9 FOREIGN KEY (parent_subject_id) REFERENCES subject(id);


--
-- Name: fkc9f42bc1cab6b3e1; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY level_permission_sixth
    ADD CONSTRAINT fkc9f42bc1cab6b3e1 FOREIGN KEY (level_permission_id) REFERENCES permission(id);


--
-- Name: fkca019652cab6b3e1; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY level_permission_third
    ADD CONSTRAINT fkca019652cab6b3e1 FOREIGN KEY (level_permission_id) REFERENCES permission(id);


--
-- Name: fkcacdcff2239fd3b8; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY status
    ADD CONSTRAINT fkcacdcff2239fd3b8 FOREIGN KEY (url_id) REFERENCES url(id);


--
-- Name: fkcacdcff2b83749f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY status
    ADD CONSTRAINT fkcacdcff2b83749f FOREIGN KEY (owner_id) REFERENCES profile_base(id);


--
-- Name: fkcc0c1247b5fe38a4; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY clinical_assay_instance
    ADD CONSTRAINT fkcc0c1247b5fe38a4 FOREIGN KEY (assay_id) REFERENCES clinical_assay(id);


--
-- Name: fkdc46c9ab999ed6d1; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY compound
    ADD CONSTRAINT fkdc46c9ab999ed6d1 FOREIGN KEY (compound_id) REFERENCES term(id);


--
-- Name: fke125c5cf123bda38; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT fke125c5cf123bda38 FOREIGN KEY (group_id) REFERENCES _group(id);


--
-- Name: fke125c5cf6b2175ad; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT fke125c5cf6b2175ad FOREIGN KEY (user_id) REFERENCES _user(id);


--
-- Name: fke125c5cfac8a955c; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT fke125c5cfac8a955c FOREIGN KEY (role_id) REFERENCES _role(id);


--
-- Name: fked74638013c6ed3e; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY social_media_account
    ADD CONSTRAINT fked74638013c6ed3e FOREIGN KEY (profile_id) REFERENCES url(id);


--
-- Name: fked746380950ad505; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY social_media_account
    ADD CONSTRAINT fked746380950ad505 FOREIGN KEY (service_id) REFERENCES social_media_service(id);


--
-- Name: fked746380b83749f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY social_media_account
    ADD CONSTRAINT fked746380b83749f FOREIGN KEY (owner_id) REFERENCES profile_base(id);


--
-- Name: fkef124b114368590; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY feature_base_term
    ADD CONSTRAINT fkef124b114368590 FOREIGN KEY (feature_base_organism_part_id) REFERENCES feature_base(id);


--
-- Name: fkef124b114ec66994; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY feature_base_term
    ADD CONSTRAINT fkef124b114ec66994 FOREIGN KEY (feature_base_disease_id) REFERENCES feature_base(id);


--
-- Name: fkef124b11731cd870; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY feature_base_term
    ADD CONSTRAINT fkef124b11731cd870 FOREIGN KEY (term_id) REFERENCES term(id);


--
-- Name: fkef124b1182c4dce6; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY feature_base_term
    ADD CONSTRAINT fkef124b1182c4dce6 FOREIGN KEY (feature_base_enzyme_id) REFERENCES feature_base(id);


--
-- Name: fkef124b11b004c3e6; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY feature_base_term
    ADD CONSTRAINT fkef124b11b004c3e6 FOREIGN KEY (feature_base_metabolite_id) REFERENCES feature_base(id);


--
-- Name: fkef124b11cf1c4d46; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY feature_base_term
    ADD CONSTRAINT fkef124b11cf1c4d46 FOREIGN KEY (feature_base_drug_id) REFERENCES feature_base(id);


--
-- Name: fkef124b11f3d55ddb; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY feature_base_term
    ADD CONSTRAINT fkef124b11f3d55ddb FOREIGN KEY (feature_base_compound_id) REFERENCES feature_base(id);


--
-- Name: fkf43101e7d70825c5; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY login_record
    ADD CONSTRAINT fkf43101e7d70825c5 FOREIGN KEY (owner_id) REFERENCES _user(id);


--
-- Name: fkf9ebc2a1953ed7f; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY sample_template_field
    ADD CONSTRAINT fkf9ebc2a1953ed7f FOREIGN KEY (template_field_id) REFERENCES template_field(id);


--
-- Name: fkf9ebc2a83649af6; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY sample_template_field
    ADD CONSTRAINT fkf9ebc2a83649af6 FOREIGN KEY (sample_system_fields_id) REFERENCES sample(id);


--
-- Name: fkfc7c268b675e4f5; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_person
    ADD CONSTRAINT fkfc7c268b675e4f5 FOREIGN KEY (role_id) REFERENCES person_role(id);


--
-- Name: fkfc7c268bb7959aa0; Type: FK CONSTRAINT; Schema: public; Owner: gscf
--

ALTER TABLE ONLY study_person
    ADD CONSTRAINT fkfc7c268bb7959aa0 FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

