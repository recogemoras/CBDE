------- SCHEMA  -----------

CREATE TABLE customer(
    C_CustKey int NULL,
    C_Name varchar2(64) NULL,
    C_Address varchar2(64) NULL,
    C_NationKey int NULL,
    C_Phone varchar2(64) NULL,
    C_AcctBal number(13, 2) NULL,
    C_MktSegment varchar2(64) NULL,
    C_Comment varchar2(120) NULL,
    skip varchar2(64) NULL
);

CREATE TABLE lineitem(
    L_OrderKey int NULL,
    L_PartKey int NULL,
    L_SuppKey int NULL,
    L_LineNumber int NULL,
    L_Quantity int NULL,
    L_ExtendedPrice number(13, 2) NULL,
    L_Discount number(13, 2) NULL,
    L_Tax number(13, 2) NULL,
    L_ReturnFlag varchar2(64) NULL,
    L_LineStatus varchar2(64) NULL,
    L_ShipDate date NULL,
    L_CommitDate date NULL,
    L_ReceiptDate date NULL,
    L_ShipInstruct varchar2(64) NULL,
    L_ShipMode varchar2(64) NULL,
    L_Comment varchar2(64) NULL,
    skip varchar2(64) NULL
)
PARTITION BY RANGE (L_ShipDate) (
PARTITION L_ShipDate1 VALUES LESS THAN (TO_DATE('21-04-2013', 'DD-MM-YYYY')),
PARTITION L_ShipDate2 VALUES LESS THAN (TO_DATE('01-05-2013', 'DD-MM-YYYY')),
PARTITION L_ShipDate3 VALUES LESS THAN (MAXVALUE))
;

CREATE TABLE nation(
    N_NationKey int NULL,
    N_Name varchar2(64) NULL,
    N_RegionKey int NULL,
    N_Comment varchar2(160) NULL,
    skip varchar2(64) NULL
);

CREATE TABLE orders(
    O_OrderKey int NULL,
    O_CustKey int NULL,
    O_OrderStatus varchar2(64) NULL,
    O_TotalPrice number(13, 2) NULL,
    O_OrderDate date NULL,
    O_OrderPriority varchar2(15) NULL,
    O_Clerk varchar2(64) NULL,
    O_ShipPriority int NULL,
    O_Comment varchar2(80) NULL,
    skip varchar2(64) NULL
)
PARTITION BY RANGE (O_OrderDate) (
PARTITION O_OrderDate1 VALUES LESS THAN (TO_DATE('30-04-2013', 'DD-MM-YYYY')),
PARTITION O_OrderDate2 VALUES LESS THAN (TO_DATE('30-04-2014', 'DD-MM-YYYY')),
PARTITION O_OrderDate3 VALUES LESS THAN (MAXVALUE))
;

CREATE TABLE part(
    P_PartKey int NULL,
    P_Name varchar2(64) NULL,
    P_Mfgr varchar2(64) NULL,
    P_Brand varchar2(64) NULL,
    P_Type varchar2(64) NULL,
    P_Size int NULL,
    P_Container varchar2(64) NULL,
    P_RetailPrice number(13, 2) NULL,
    P_Comment varchar2(64) NULL,
    skip varchar2(64) NULL
);

CREATE TABLE partsupp(
    PS_PartKey int NULL,
    PS_SuppKey int NULL,
    PS_AvailQty int NULL,
    PS_SupplyCost number(13, 2) NULL,
    PS_Comment varchar2(200) NULL,
    skip varchar2(64) NULL
);

CREATE TABLE region(
    R_RegionKey int NULL,
    R_Name varchar2(64) NULL,
    R_Comment varchar2(160) NULL,
    skip varchar2(64) NULL
);

CREATE TABLE supplier(
    S_SuppKey int NULL,
    S_Name varchar2(64) NULL,
    S_Address varchar2(64) NULL,
    S_NationKey int NULL,
    S_Phone varchar2(18) NULL,
    S_AcctBal number(13, 2) NULL,
    S_Comment varchar2(105) NULL,
    skip varchar2(64) NULL
);








--- INDEXES  -----------

-- Orders
create index ind_0_custkey on orders(o_custkey) pctfree 33;

-- LineItem
create index ind_l_shipdate on lineitem(l_shipdate) pctfree 33;
create index ind_l_orderkey on lineitem(l_orderkey) pctfree 33;

-- Supplier
create index ind_s_nationkey on supplier(s_nationkey) pctfree 33;

-- Customer
create index ind_c_mktsegment on customer(c_mktsegment) pctfree 33;
create index ind_c_nationkey on customer(c_nationkey) pctfree 33;

-- PartSupp
create index ind_ps_partkey on partsupp(ps_partkey) pctfree 33;

-- Part
create index ind_p_size on part(p_size) pctfree 33;

-- Nation
create index ind_n_regionkey on nation(n_regionkey) pctfree 33;