create or replace 
PACKAGE PKG_ACTIVITY_REPORT 
IS
    TYPE activity_report_item_type IS RECORD
    ( emp_id MERCHANT.merchant_id%TYPE,
    emp_name MERCHANT.MERCHANT_NAME%TYPE,
    emp_gender MERCHANT.MERCHANT_CODE%TYPE );
    TYPE activity_report_items_type IS TABLE OF activity_report_item_type INDEX BY BINARY_INTEGER;
  
    -- Procedure to retrive the activity report of given operator
    PROCEDURE enquiry_activity_report(activity_report_items OUT activity_report_items_type);
END PKG_ACTIVITY_REPORT;

create or replace 
PACKAGE BODY PKG_ACTIVITY_REPORT 
IS
PROCEDURE enquiry_activity_report (activity_report_items OUT activity_report_items_type)
IS
    activity_report_item activity_report_item_type;
BEGIN
    activity_report_item.emp_id := 300000000;
    activity_report_item.emp_name := 'Barbara';
    activity_report_item.emp_gender := 'Female';

    activity_report_items(1) := activity_report_item;

    activity_report_item.emp_id := 300000008;
    activity_report_item.emp_name := 'Rick';
    activity_report_item.emp_gender := 'Male';

    activity_report_items(2) := activity_report_item;

    FOR i IN 1..activity_report_items.count LOOP
        DBMS_OUTPUT.PUT_LINE('i='||i||', emp_id ='||activity_report_items(i).emp_id||', emp_name ='
        ||activity_report_items(i).emp_name||', emp_gender = '||activity_report_items(i).emp_gender);
    END LOOP;
END enquiry_activity_report;

END PKG_ACTIVITY_REPORT;