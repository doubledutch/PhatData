function showMenu(id){
	var newState=state.set('current_page',id)
	if(!newState.equals(state)){
		state=newState
		render()
	}
}


var taskExplorerTimer=null

function startExplorerSample(){
	var code=editor.getValue();
	$.ajax({
	  	url: "/sample/",
	  	type: "POST",
	  	data:JSON.stringify({
	  		code:code,
	  		count:$('#sample').find('input[name="count"]').val(),
	  		type:$('#sample').find('select[name="type"]').val()
	  	}),
	  	processData: false

	}).done(function(data) {
		console.log(data)
		state=state.set('sample',Immutable.fromJS(data.sample))
		render()
		taskExplorerTimer=window.setTimeout(updateExplorerSample, 1000)
		// show task list
	}).fail(function (jqXHR, textStatus) {
    	console.log('failure')
    	console.log(textStatus)
	})
}



function updateExplorerSample(){
	var sample=state.get('sample')
	if(sample.has('id')){
		$.ajax({
		  	url: "/sample/"+state.get('sample').get('id'),
		  	type: "GET"
		}).done(function(data) {
			console.log('got sample')
			console.log(data)
			var newState=state.set('sample',Immutable.fromJS(data))
			if(!newState.equals(state)){
				state=newState
				render()
			}
			if(state.get('sample').get('state')<3){
				taskExplorerTimer=window.setTimeout(updateExplorerSample, 1000)
			}
		})
	}
}
