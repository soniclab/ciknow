
CKEDITOR.editorConfig = function( config )
{
    config.uiColor="#aadc6e";
    config.resize_enabled = false;
    config.toolbar = 'ciknow';
    config.toolbar_ciknow =
    [
    [ 'Bold','Italic','Underline','Strike','Subscript','Superscript'],
    [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock' ],    
    [ 'Styles','Format','Font','FontSize' ],
    '/',
    [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ],
    [ 'Find','Replace' ],
    [ 'Link','Unlink','Anchor' ],
    [ 'Image','Flash','Table','HorizontalRule','Smiley','SpecialChar' ],  
    [ 'TextColor','BGColor' ],  
    [ 'Source'],
    ];

};


// This is actually the default value.
/*
config.toolbar_Full =
[
    { name: 'document',    items : [ 'Source','-','Save','NewPage','DocProps','Preview','Print','-','Templates' ] },
    { name: 'clipboard',   items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
    { name: 'editing',     items : [ 'Find','Replace','-','SelectAll','-','SpellChecker', 'Scayt' ] },
    { name: 'forms',       items : [ 'Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField' ] },
    '/',
    { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] },
    { name: 'paragraph',   items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','BidiLtr','BidiRtl' ] },
    { name: 'links',       items : [ 'Link','Unlink','Anchor' ] },
    { name: 'insert',      items : [ 'Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak' ] },
    '/',
    { name: 'styles',      items : [ 'Styles','Format','Font','FontSize' ] },
    { name: 'colors',      items : [ 'TextColor','BGColor' ] },
    { name: 'tools',       items : [ 'Maximize', 'ShowBlocks','-','About' ] }
];  
*/
