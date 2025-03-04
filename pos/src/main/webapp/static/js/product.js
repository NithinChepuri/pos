function searchProducts(event) {
    event.preventDefault();
    
    const form = {
        clientId: $('#inputClient').val() || null,
        name: $('#inputName').val(),
        barcode: $('#inputBarcode').val(),
        mrp: $('#inputMrp').val() || null
    };
    
    const json = JSON.stringify(form);
    
    $.ajax({
        url: '/api/products/search',
        type: 'POST',
        data: json,
        headers: {
            'Content-Type': 'application/json'
        },
        success: function(response) {
            displayProductList(response);
        },
        error: handleAjaxError
    });
}

// Add this to your init function
function init() {
    $('#search-form').submit(searchProducts);
    // ... existing init code ...
} 