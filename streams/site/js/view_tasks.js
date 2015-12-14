function renderAddTaskForm(data){
	var buf=''
	buf+='<form name="addExport" id="addExport">'
	buf+='<label>Filename</label>'
	buf+='<input type="text" name="filename" value="untitled.stream">'
	buf+='<label>Stream</label>'
	buf+='<select name="topic">'
	var streams=data.get('streams')
	for(var i=0;i<streams.size;i++){
		var stream=streams.get(i)
		if(stream.get('topic').indexOf('_phatdata.')!=0){
			buf+='<option value="'+stream.get("topic")+'">'+stream.get("topic")+'</option>'
		}
	}
	buf+='</select>'
	buf+='<input type="button" name="export" value="Export" onclick="createExportTask()">'
	buf+='</form>'
	buf+='<form name="addImport" id="addImport">'
	buf+='<label>File</label>'
	buf+='<select name="filename">'
	var files=data.get('importFiles')
	for(var i=0;i<files.size;i++){
		var file=files.get(i)
		buf+='<option value="'+file.get("filename")+'">'+file.get("filename")+'</option>'
	}
	buf+='</select>'
	buf+='<label>Stream</label>'
	/*buf+='<select name="topic">'
	var streams=data.get('streams')
	for(var i=0;i<streams.size;i++){
		var stream=streams.get(i)
		if(stream.get('topic').indexOf('_phatdata.')!=0){
			buf+='<option value="'+stream.get("topic")+'">'+stream.get("topic")+'</option>'
		}
	}
	buf+='</select>'*/
	buf+='<input type="text" name="topic" value="">'
	buf+='<input type="button" name="import" value="Import" onclick="createImportTask()">'
	buf+='</form>'
	return buf
}

function renderTask(task){
	// {"checkpoint":0,"filename":"untitled.stream","topic":"foo","started":-1,"finished":-1,"id":"1f165bc1-e530-44f8-8498-fc3eeee8f6ec","state":0,"type":1}
	var buf=''
	buf+='<div class="task"><h1>'
	if(task.get('type')==0){
		buf+='Import'
	}else{
		buf+='Export'
	}
	buf+=' <small>'
	if(task.get('state')==0){
		buf+='created'
	}else if(task.get('state')==1){
		buf+='running'
	}else if(task.get('state')==2){
		buf+='done'
	}else if(task.get('state')==3){
		buf+='failed'
	}
	buf+='</small>'
	buf+='</h1><p>stream:'+task.get('topic')+' filename:'+task.get('filename')
	buf+=' checkpoint:'+task.get('checkpoint')
	buf+='</p>'
	if(task.get('state')==1){
		buf+='<div style="width:400px;border:1px solid #AAAAAA;background:#EEEEEE;">'
		buf+='<div style="width:'
		buf+=Math.floor(task.get('completion')*398)
		buf+='px;height:10px;background:#ff1170;">'
		// buf+=task.get('completion')
		buf+='</div>'
		buf+='</div>'
	}
	buf+='</div>'
	return buf
}

function renderTaskList(data){
	var tasks=data.get('tasks')
	var buf=''
	for(var i=0;i<tasks.size;i++){
		var task=tasks.get(i)
		buf+=renderTask(task)
	}
	return buf
}

function renderTasksPage(data){
	var buf=''
	buf+=renderAddTaskForm(data)
	buf+=renderTaskList(data)
	$('#content').html(buf)
}