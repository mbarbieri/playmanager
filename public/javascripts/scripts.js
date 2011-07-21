$(document).ready(function() {
	$('.showlog').colorbox({speed:300});

    $('#deploymentTypeSelector').change(function(){
        if($(this).val() == 0) {
            $('#zipDeployment').show();
            $('#gitDeployment').hide();
        }
        else if($(this).val() == 1) {
            $('#zipDeployment').hide();
            $('#gitDeployment').show();
        }
    });
});