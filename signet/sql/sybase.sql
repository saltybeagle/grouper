-- This is the Sybase DDL for the Signet database
-- Author Lynn Mcrae, Stanford University 
-- modified
--    6/20/2005 - add assignment expirationDate
--
-- Signet Subject tables
drop table signet_privilegedSubject;
-- Tree tables
drop table signet_treeNodeRelationship;
drop table signet_treeNode;
drop table signet_tree;
-- ChoiceSet tables
drop table signet_choice;
drop table signet_choiceSet;
-- Assignment tables
drop table signet_assignmentLimit_history;
drop table signet_assignment_history;
drop table signet_assignmentLimit;
drop table signet_assignment;
drop table signet_proxy_history;
drop table signet_proxy;
-- Subsystem tables
drop table signet_permission_limit;
drop table signet_function_permission;
drop table signet_category;
drop table signet_function;
drop table signet_permission;
drop table signet_limit;
drop table signet_subsystem;
-- Subject tables (optional, for local subject tables)
drop table SubjectAttribute;
drop table Subject;
drop table SubjectType;
--
-- Subsystem tables
create table signet_subsystem
(
subsystemID         varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NOT NULL,
scopeTreeID         varchar(64)         NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key clustered (subsystemID)
)
;
create table signet_category
(
subsystemID         varchar(64)         NOT NULL,
categoryID          varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (subsystemID, categoryID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_function
(
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
categoryID          varchar(64)         NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (subsystemID, functionID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_permission
(
permissionKey		numeric(12,0)       IDENTITY,
subsystemID         varchar(64)         NOT NULL,
permissionID        varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (permissionKey),
unique (subsystemID, permissionID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_limit
(
limitKey			numeric(12,0)       IDENTITY,
subsystemID         varchar(64)         NOT NULL,
limitID             varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
limitType           varchar(16)         NOT NULL,
limitTypeID         varchar(64)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NULL,
dataType            varchar(32)         NOT NULL,
valueType           varchar(32)         NOT NULL,
displayOrder        smallint            NOT NULL,
renderer            varchar(255)        NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (limitKey),
unique (subsystemID, limitID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_function_permission
(
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
permissionKey       numeric(12,0)       NOT NULL,
primary key (subsystemID, functionID, permissionKey),
foreign key (subsystemID, functionID) references signet_function (subsystemID, functionID),
foreign key (permissionKey) references signet_permission (permissionKey)
)
;
create table signet_permission_limit
(
permissionKey       numeric(12,0)       NOT NULL,
limitKey            numeric(12,0)       NOT NULL,
defaultLimitValueValue  varchar(64)     NULL,
primary key (permissionKey, limitKey),
foreign key (permissionKey) references signet_permission (permissionKey),
foreign key (limitKey) references signet_limit (limitKey)
)
;
-- Signet Subject tables
create table signet_privilegedSubject (
subjectTypeID       varchar(32)         NOT NULL,
subjectID           varchar(64)         NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (subjectTypeID, subjectID)
)
;
-- Tree tables
create table signet_tree
(
treeID              varchar(64)         NOT NULL,
name                varchar(120)        NOT NULL,
adapterClass        varchar(255)        NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (treeID)
)
;
create table signet_treeNode
(
treeID              varchar(64)         NOT NULL,
nodeID              varchar(64)         NOT NULL,
nodeType            varchar(32)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (treeID, nodeID),
foreign key (treeID, nodeID) references signet_treeNode (treeID, nodeID)
)
;
create table signet_treeNodeRelationship
(
treeID              varchar(64)         NOT NULL,
nodeID              varchar(64)         NOT NULL,
parentNodeID        varchar(64)         NOT NULL,
primary key (treeID, nodeID, parentNodeID),
foreign key (treeID) references signet_tree (treeID)
)
;
-- ChoiceSet tables
create table signet_choiceSet
(
choiceSetID         varchar(64)         NOT NULL,
adapterClass        varchar(255)        NOT NULL,
subsystemID         varchar(64)         NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (choiceSetID)
)
;
create table signet_choice
(
choiceSetID         varchar(64)         NOT NULL,
value               varchar(32)         NOT NULL,
label               varchar(64)         NOT NULL,
rank                smallint            NOT NULL,
displayOrder        smallint            NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (choiceSetID, value),
foreign key (choiceSetID) references signet_choiceSet (choiceSetID)
)
;
-- Assignment tables
create table signet_assignment
(
assignmentID        numeric(12,0)       IDENTITY,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
grantorTypeID       varchar(32)         NOT NULL,
grantorID           varchar(64)         NOT NULL,
granteeTypeID       varchar(32)         NOT NULL,
granteeID           varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NULL,
proxyID             varchar(64)         NULL,
scopeID             varchar(64)         NULL,
scopeNodeID         varchar(64)         NULL,
canUse              bit                 NOT NULL,
canGrant            bit                 NOT NULL,
effectiveDate       smalldatetime       NOT NULL,
expirationDate      smalldatetime       NULL,
revokerTypeID       varchar(32)         NULL,
revokerID           varchar(64)         NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (assignmentID)
)
;
create table signet_assignmentLimit
(
assignmentID        numeric(12,0)       NOT NULL,
limitKey    		numeric(12,0)       NOT NULL,
value               varchar(32)         NOT NULL,
unique (assignmentID, limitKey, value),
foreign key (assignmentID) references signet_assignment (assignmentID),
foreign key (limitKey) references signet_limit (limitKey)
)
;
create table signet_assignment_history
(
historyID           numeric(12,0)       IDENTITY,
assignmentID        numeric(12,0)       NOT NULL,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
grantorTypeID       varchar(32)         NOT NULL,
grantorID           varchar(64)         NOT NULL,
granteeTypeID       varchar(32)         NOT NULL,
granteeID           varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NULL,
proxyID             varchar(64)         NULL,
scopeID             varchar(64)         NULL,
scopeNodeID         varchar(64)         NULL,
canUse              bit                 NOT NULL,
canGrant            bit                 NOT NULL,
effectiveDate       smalldatetime       NOT NULL,
expirationDate      smalldatetime       NULL,
revokerTypeID       varchar(32)         NULL,
revokerID           varchar(64)         NULL,
historyDatetime     smalldatetime       NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (assignmentID, instanceNumber)
)
;
create table signet_assignmentLimit_history
(
historyID			numeric(12,0)		IDENTITY,
assignmentID        numeric(12,0)       NOT NULL,
instanceNumber      int                 NOT NULL,
limitSubsystemID    varchar(64)         NOT NULL,
-- limitType is so far unused, but will eventually indicate whether this
-- Limit is a Tree or a ChoiceSet. For the moment, only a ChoiceSet is
-- possible.
limitType           varchar(32)         NOT NULL,
-- limitTypeId is the ID of the ChoiceSet or Tree whose values/nodes are the
-- domain of limit values, and this ID is unique only within its limitType.
-- That is, it's possible to have a Limit of limitType "Tree" with id "foo",
-- and another Limit of limitType "ChoiceSet" with id "foo".
limitTypeID         varchar(64)         NOT NULL,
value               varchar(32)         NOT NULL,
primary key(historyID),
foreign key(assignmentID, instanceNumber)
  references signet_assignment_history(assignmentID, instanceNumber)
)
;
create table signet_proxy
(
proxyID             numeric(12,0)       IDENTITY,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NULL,
grantorTypeID       varchar(32)         NOT NULL,
grantorID           varchar(64)         NOT NULL,
granteeTypeID       varchar(32)         NOT NULL,
granteeID           varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NULL,
proxySubjectID		varchar(64)			NULL,
canUse              bit                 NOT NULL,
canExtend           bit                 NOT NULL,
effectiveDate       smalldatetime       NOT NULL,
expirationDate      smalldatetime       NULL,
revokerTypeID       varchar(32)         NULL,
revokerID           varchar(64)         NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (proxyID)
)
;
create table signet_proxy_history
(
historyID           numeric(12,0)       IDENTITY,
proxyID             numeric(12,0)       NOT NULL,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NULL,
grantorTypeID       varchar(32)         NOT NULL,
grantorID           varchar(64)         NOT NULL,
granteeTypeID       varchar(32)         NOT NULL,
granteeID           varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NULL,
proxySubjectID		varchar(64)			NULL,
canUse              bit                 NOT NULL,
canExtend           bit                 NOT NULL,
effectiveDate       smalldatetime       NOT NULL,
expirationDate      smalldatetime       NULL,
revokerTypeID       varchar(32)         NULL,
revokerID           varchar(64)         NULL,
historyDatetime     smalldatetime       NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (proxyID, instanceNumber)
)
;
-- Subject tables (optional, for local subject tables)
create table SubjectType (
subjectTypeID     varchar(32)     NOT NULL,
name              varchar(120)    NOT NULL,
adapterClass      varchar(255)    NOT NULL,
modifyDatetime    smalldatetime   default getdate(),
primary key (subjectTypeID)
)
;
create table Subject
(
subjectTypeID     varchar(32)     NOT NULL,
subjectID         varchar(64)     NOT NULL,
name              varchar(120)    NOT NULL,
description       varchar(255)    NOT NULL,
displayID         varchar(64)     NOT NULL,
modifyDatetime    smalldatetime   default getdate(),
primary key (subjectTypeID, subjectID)
)
;
create table SubjectAttribute
(
subjectTypeID     varchar(32)     NOT NULL,
subjectID         varchar(64)     NOT NULL,
name              varchar(32)     NOT NULL,
instance          smallint        NOT NULL,
value             varchar(255)    NOT NULL,
searchValue       varchar(255)    NOT NULL,
modifyDatetime    smalldatetime   default getdate(),
primary key (subjectTypeID, subjectID, name, instance),
foreign key (subjectTypeID, subjectID) references Subject (subjectTypeID, subjectID)
)
;

