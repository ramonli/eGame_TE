# active by multiple book numbers
def activebycriteria_bymultiplebooks():
    return \
    """
    <Criteria value="456123789,198415681" type="4"/>
    """
     
# active by first ticket serial number in a book
def activebycriteria_byfirstticket():    
    return \
    """
    <Criteria value="198415681000" type="3"/>
    """
    
# active by last ticket serial number in a book
def activebycriteria_bylastticket():    
    return \
    """
    <Criteria value="198415681199" type="1"/>
    """    

# active by range ticket serial number in a book
def activebycriteria_byrangeticket():    
    return \
    """
    <Criteria value="198415681001,198415681010" type="2"/>
    """
    
# active by range ticket serial number in a book
def activebycriteria_bybatchbook():    
    return \
    """
    <Criteria value="198415681,200415681,157823119" type="4"/>
    """
    
# active by range ticket serial number in a book
def activebycriteria_bybatchrange():    
    return \
    """
    <Criteria value="198415681001-198415681010,157823119020-157823119025,200415681002-200415681100" type="5"/>
    """        
    
def batch_validate():
    return \
    """
    <BatchPayout>
        <Payout actualAmount="400000" inputChannel="1">
            <Ticket serialNo="157823119021" VIRN="95497797"/>
        </Payout> 
        <Payout actualAmount="21111" inputChannel="1">
            <Ticket serialNo="198415681983" VIRN="37330200"/>
        </Payout>
        <Payout actualAmount="31111" inputChannel="1">
            <Ticket serialNo="598198195103" VIRN="27330200"/>
        </Payout>
    </BatchPayout>
    """
    
    
    