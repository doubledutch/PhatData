var editor =ace.edit("editor")
editor.setTheme("ace/theme/chrome");
editor.getSession().setMode("ace/mode/javascript")
// editor.setValue(data.get('explorer').get('editorContent'))

var state=Immutable.fromJS({
	pages:[
		{title:'Data Explorer'}//,
	//	{title:'Time Series'},
	//	{title:'Aggregates'},
	//	{title:'Hash Tables'}
	],
	current_page:0,
	explorer:{},
	sample:{}
})

render()