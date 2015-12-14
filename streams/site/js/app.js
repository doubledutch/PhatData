var state=Immutable.fromJS({
	pages:[
		// {title:'Dashboard'},
		{title:'Streams'},
		{title:'Tasks'}
	],
	current_page:0,
	streams:[],
	tasks:[],
	importFiles:[]
})

render()

var streamTimer=null

function updateStreamData(){
	$.ajax({
	  	url: "/stream/"
	}).done(function(data) {
		var list=Immutable.fromJS(data)
		var newState=state.set('streams', list)
		if(!newState.equals(state)){
			state=newState
			render()
		}
		streamTimer=window.setTimeout(updateStreamData, 2000)
	});
}
updateStreamData()

var taskTimer=null

function updateTaskData(){
	$.ajax({
	  	url: "/task/"
	}).done(function(data) {
		var list=Immutable.fromJS(data.tasks)
		var newState=state.set('tasks', list)
		list=Immutable.fromJS(data.files)
		var newState=newState.set('importFiles', list)
		if(!newState.equals(state)){
			state=newState
			render()
		}
		taskTimer=window.setTimeout(updateTaskData, 2000)
	});
}
updateTaskData()