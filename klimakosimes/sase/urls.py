from django.urls import path
from . import views

urlpatterns = [
    path('', views.home, name="home"),
    path('initiate_sase/', views.initiate_sase, name='initiate_sase'),
    path('sase_result/', views.sase_result, name='sase_result'),
    path('run_docker_compose/', views.run_docker_compose, name='run_docker_compose'),
]