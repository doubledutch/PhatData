function showMenu(id){
	var newState=state.set('current_page',id)
	if(!newState.equals(state)){
		state=newState
		render()
	}
}

function createExportTask(){
	$.ajax({
	  	url: "/task/",
	  	type: "POST",
	  	data:JSON.stringify({
	  		type:1,
	  		filename:$('#addExport').find('input[name="filename"]').val(),
	  		topic:$('#addExport').find('select[name="topic"]').val()
	  	}),
	  	processData: false

	}).done(function(data) {
		console.log('task was created')
		// show task list
	}).fail(function (jqXHR, textStatus) {
    	console.log('failure')
    	console.log(textStatus)
	})
}

function createImportTask(){
	$.ajax({
	  	url: "/task/",
	  	type: "POST",
	  	data:JSON.stringify({
	  		type:0,
	  		filename:$('#addImport').find('select[name="filename"]').val(),
	  		topic:$('#addImport').find('input[name="topic"]').val()
	  	}),
	  	processData: false

	}).done(function(data) {
		// console.log('task was created')
		// show task list
		window.clearTimeout(taskTimer)
		updateTaskData()
	}).fail(function (jqXHR, textStatus) {
    	console.log('failure')
    	console.log(textStatus)
	})
}

/*
function createCheckpoint(id){
	$.ajax({
	  	url: "/stream/"+id,
	  	type: "POST"

	}).done(function(data) {
		//figure out checkpoint api
		//create ui for checkpoint title entry
		//create endpoint
		//post to checkpoints, trigger data fetch
	})
	return false
}*/